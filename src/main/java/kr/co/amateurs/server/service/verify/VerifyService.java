package kr.co.amateurs.server.service.verify;

import kr.co.amateurs.server.domain.dto.verify.PythonServiceResponseDTO;
import kr.co.amateurs.server.domain.dto.verify.VerifyResultDTO;
import kr.co.amateurs.server.domain.dto.verify.VerifyStatusDTO;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.verify.Verify;
import kr.co.amateurs.server.domain.entity.verify.VerifyStatus;
import kr.co.amateurs.server.domain.event.VerificationEvent;
import kr.co.amateurs.server.repository.verify.VerifyRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ApplicationEventPublisher eventPublisher;

    @Value("${verification.service.url}")
    private String verificationServiceUrl;

    @Value("${verification.service.endpoint}")
    private String verificationEndpoint;

    @Transactional
    public VerifyResultDTO verifyStudent(User user, MultipartFile image, DevCourseTrack devcourseName, String devcourseBatch) {
        validateUserForVerification(user);

        try {
            String imageUrl = uploadImageToS3(image);

            log.info("이미지 업로드 완료");
            Verify verify = Verify.builder()
                    .user(user)
                    .status(VerifyStatus.PROCESSING)
                    .imageUrl(imageUrl)
                    .verifiedAt(LocalDateTime.now())
                    .ocrScore(0)
                    .layoutScore(0)
                    .totalScore(0)
                    .build();


            Verify savedVerify = verifyRepository.save(verify);
            log.info("인증 요청 생성: verifyId={}, userId={}", savedVerify.getId(), user.getId());

            eventPublisher.publishEvent(new VerificationEvent(
                    savedVerify.getId(),
                    imageUrl,
                    image.getOriginalFilename(),
                    user,
                    devcourseName,
                    devcourseBatch
            ));

            return VerifyResultDTO.processing();

        } catch (IOException e) {
            throw ErrorCode.FILE_PROCESSING_ERROR.get();
        } catch (Exception e) {
            throw ErrorCode.VERIFICATION_PROCESSING_ERROR.get();
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVerificationEvent(VerificationEvent event) {
        log.info("트랜잭션 커밋 후 비동기 처리 시작: verifyId={}", event.verifyId());

        CompletableFuture.runAsync(() -> {
            processVerificationAsync(
                    event.verifyId(),
                    event.imageUrl(),
                    event.filename(),
                    event.user(),
                    event.devcourseName(),
                    event.devcourseBatch()
            );
        });
    }

    @Transactional
    public void processVerificationAsync(Long verifyId, String imageUrl, String filename, User user, DevCourseTrack devcourseName, String devcourseBatch) {
        try {
            log.info("비동기 인증 처리 시작: verifyId={}", verifyId);
            Verify verify = verifyRepository.findById(verifyId)
                    .orElseThrow(() -> ErrorCode.NOT_FOUND.get());


            PythonServiceResponseDTO.DataDTO data = callPythonVerificationServiceByUrl(imageUrl, filename);

            VerifyStatus status = determineStatusAndUpdateRole(user, data.totalScore(), devcourseName, devcourseBatch);

            verify.updateVerification(data, status);
            verifyRepository.save(verify);

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
                verify.updateToFailed(errorMessage);
                verifyRepository.save(verify);
            });
        } catch (Exception e) {
            log.error("실패 상태 업데이트 실패: verifyId={}", verifyId, e);
        }
    }

    /**
     * Python 인증 서비스 - URL 전달
     * @param
     */
    private PythonServiceResponseDTO.DataDTO callPythonVerificationServiceByUrl(String imageUrl, String filename) {
        String url = verificationServiceUrl + verificationEndpoint;

        log.info("Python 서비스 URL 호출: {}, imageUrl: {}, filename: {}", url, imageUrl, filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of(
                "imageUrl", imageUrl,
                "filename", filename != null ? filename : "image.jpg"
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

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

    private VerifyStatus determineStatusAndUpdateRole(User user, int totalScore, DevCourseTrack devcourseName, String devcourseBatch) {
        if (totalScore >= 85) {
            userService.changeUserRole(user, Role.STUDENT);
            userService.updateDevCourseInfo(user, devcourseName, devcourseBatch);
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

    public VerifyStatusDTO getVerificationStatus(User user) {
        if (user.getRole() != Role.GUEST) {
            return VerifyStatusDTO.completed(user.getRole());
        }

        Optional<Verify> latestVerify = verifyRepository.findFirstByUserOrderByCreatedAtDesc(user);

        if (latestVerify.isPresent()) {
            Verify verify = latestVerify.get();
            return VerifyStatusDTO.fromVerify(user.getRole(), verify.getStatus());
        } else {
            // 인증 기록 없음
            return VerifyStatusDTO.notRequested(user.getRole());
        }
    }
}