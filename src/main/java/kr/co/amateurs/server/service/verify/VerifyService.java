package kr.co.amateurs.server.service.verify;

import kr.co.amateurs.server.domain.dto.verify.PythonServiceResponseDTO;
import kr.co.amateurs.server.domain.dto.verify.VerifyMapper;
import kr.co.amateurs.server.domain.dto.verify.VerifyResultDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.verify.Verify;
import kr.co.amateurs.server.domain.entity.verify.VerifyStatus;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.repository.verify.VerifyRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import kr.co.amateurs.server.domain.common.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyService {
    private final RestTemplate restTemplate;
    private final VerifyRepository verifyRepository;
    private final FileService fileService;
    private final UserService userService;

    @Value("${verification.service.url}")
    private String verificationServiceUrl;

    @Value("${verification.service.endpoint}")
    private String verificationEndpoint;

    @Transactional
    public VerifyResultDTO verifyStudent(User user, MultipartFile image) {
        validateUserForVerification(user);

        try {
            String imageUrl = uploadImageToS3(image);

            Verify verify = Verify.builder()
                    .user(user)
                    .status(VerifyStatus.PROCESSING)
                    .imageUrl(imageUrl)
                    .verifiedAt(LocalDateTime.now())
                    .build();

            verifyRepository.save(verify);

            CompletableFuture.runAsync(() -> {
                processVerificationAsync(verify.getId(), image);
            });

            return VerifyResultDTO.processing();

        } catch (IOException e) {
        throw ErrorCode.FILE_PROCESSING_ERROR.get();
        } catch (Exception e) {
        throw ErrorCode.VERIFICATION_PROCESSING_ERROR.get();
        }
    }

    @Transactional
    public void processVerificationAsync(Long verifyId, MultipartFile image) {
        try {
            log.info("비동기 인증 처리 시작: verifyId={}", verifyId);

            Verify verify = verifyRepository.findById(verifyId)
                    .orElseThrow(() -> ErrorCode.NOT_FOUND.get());
            User user = verify.getUser();

            PythonServiceResponseDTO.DataDTO data = callPythonVerificationService(image);

            VerifyStatus status = determineStatusAndUpdateRole(user, data.totalScore());

            Verify updatedVerify = VerifyMapper.updateEntity(verify, data, status);
            verifyRepository.save(updatedVerify);

            log.info("비동기 인증 처리 완료: verifyId={}, status={}", verifyId, status);

        } catch (Exception e) {
            log.error("비동기 인증 처리 실패: verifyId={}", verifyId, e);
            updateVerifyToFailed(verifyId, e.getMessage());
        }
    }

    @Transactional
    public void updateVerifyToFailed(Long verifyId, String errorMessage) {
        try {
            verifyRepository.findById(verifyId).ifPresent(verify -> {
                Verify failedVerify = VerifyMapper.updateToFailed(verify, errorMessage);
                verifyRepository.save(failedVerify);
            });
        } catch (Exception e) {
            log.error("실패 상태 업데이트 실패: verifyId={}", verifyId, e);
        }
    }

    /**
     * Python 인증 서비스 호출
     */
    private PythonServiceResponseDTO.DataDTO callPythonVerificationService(MultipartFile image) throws IOException {
        String url = verificationServiceUrl + verificationEndpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", MultipartInputStreamFileResource.from(image));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<PythonServiceResponseDTO> response = restTemplate.postForEntity(
                url, requestEntity, PythonServiceResponseDTO.class
        );

        PythonServiceResponseDTO responseBody = response.getBody();

        if (responseBody == null || !responseBody.success()) {
            throw ErrorCode.VERIFICATION_SERVICE_ERROR.get();
        }

        return responseBody.data();
    }


    public void validateUserForVerification(User user) {
        if (user.getRole() != Role.GUEST) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
        if (verifyRepository.existsByUserAndStatusIn(user,
                List.of(VerifyStatus.COMPLETED, VerifyStatus.PENDING, VerifyStatus.PROCESSING))) {
            throw ErrorCode.DUPLICATION_VERIFICATION.get();
        }
    }

    private VerifyStatus determineStatusAndUpdateRole(User user, int totalScore) {
        if (totalScore >= 85) {
            userService.changeUserRole(user, Role.STUDENT);
            return VerifyStatus.COMPLETED;
        } else if (totalScore >= 65) {
            return VerifyStatus.PENDING;
        } else {
            return VerifyStatus.FAILED;
        }
    }

    private String uploadImageToS3(MultipartFile image) throws IOException {
        return fileService.uploadFile(image, "verify-images").url();
    }
}
