package kr.co.amateurs.server.controller.verify;

import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.verify.VerifyResultDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.verify.VerifyStatus;
import kr.co.amateurs.server.repository.verify.VerifyRepository;
import kr.co.amateurs.server.service.verify.VerifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
public class VerifyController {
    private final VerifyService verifyService;
    private final VerifyRepository verifyRepository;

    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VerifyResultDTO> requestVerification(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("image") MultipartFile image) {

        User user = currentUser.getUser();

        VerifyResultDTO result = verifyService.verifyStudent(user, image);
        return ResponseEntity.ok(result);
    }
}
