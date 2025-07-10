package kr.co.amateurs.server.controller.verify;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.domain.dto.verify.VerifyResultDTO;
import kr.co.amateurs.server.service.verify.VerifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
@Tag(name = "Verify", description = "수강생 인증 API")
public class VerifyController {

    private final VerifyService verifyService;

    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "수강생 인증 요청", description = "이미지를 업로드하여 수강생 인증을 요청합니다")
    public ResponseEntity<VerifyResultDTO> requestVerification(
            @RequestParam("image") MultipartFile image) {
        
        VerifyResultDTO result = verifyService.verifyStudent(image);
        return ResponseEntity.ok(result);
    }
}
