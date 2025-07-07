package kr.co.amateurs.server.controller.verification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import kr.co.amateurs.server.domain.dto.verfication.VerificationDTO;
import kr.co.amateurs.server.service.verification.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping(value = "/student", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "수강생 인증", description = "이미지를 업로드하여 데브코스 수강생 인증을 진행합니다.")
    public ResponseEntity<VerificationDTO> verifyStudent(
            @Parameter(description = "수료증 이미지 파일",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("image") MultipartFile image) {

        // 간단한 파일 체크
        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(VerificationDTO.builder()
                            .isValid(false)
                            .message("이미지 파일이 필요합니다.")
                            .build());
        }

        VerificationDTO result = verificationService.verifyStudent(image);
        return ResponseEntity.ok(result);
    }
}