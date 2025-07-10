package kr.co.amateurs.server.service.verify;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.verify.PythonServiceResponseDTO;
import kr.co.amateurs.server.domain.dto.verify.VerifyResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyService {

    private final RestTemplate restTemplate;

    @Value("${verification.service.url}")
    private String verificationServiceUrl;

    @Value("${verification.service.endpoint}")
    private String verificationEndpoint;

    public VerifyResultDTO verifyStudent(MultipartFile image) {
        String url = verificationServiceUrl + verificationEndpoint;
        
        ByteArrayResource fileResource = convertToByte(image);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", fileResource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            log.info("Python 서비스 호출: {}", url);
            ResponseEntity<PythonServiceResponseDTO> response = restTemplate.postForEntity(
                url, requestEntity, PythonServiceResponseDTO.class);
            return handlePythonServiceResponse(response);
            
        } catch (Exception e) {
            log.error("Python 인증 서비스 호출 실패", e);
            throw ErrorCode.VERIFICATION_SERVICE_ERROR.get();
        }
    }

    private ByteArrayResource convertToByte(MultipartFile image) {
        try {
            return new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생", e);
            throw ErrorCode.VERIFICATION_SERVICE_ERROR.get();
        }
    }


    private VerifyResultDTO handlePythonServiceResponse(ResponseEntity<PythonServiceResponseDTO> response) {
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("Python 서비스 응답 오류: {}", response.getStatusCode());
            throw ErrorCode.VERIFICATION_SERVICE_ERROR.get();
        }

        PythonServiceResponseDTO responseBody = response.getBody();
        
        if (!responseBody.isSuccess()) {
            log.error("Python 서비스에서 인증 실패: {}", responseBody.getError());
            throw ErrorCode.VERIFICATION_FAILED.get();
        }

        return VerifyResultDTO.builder()
                .ocrScore(responseBody.getOcrScore())
                .layoutScore(responseBody.getLayoutScore())
                .totalScore(responseBody.getTotalScore())
                .extractedText(responseBody.getExtractedText())
                .detailMessage(responseBody.getDetailMessage())
                .isVerified(responseBody.isVerified())
                .build();
    }
} 