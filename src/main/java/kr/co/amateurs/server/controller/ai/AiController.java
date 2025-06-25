package kr.co.amateurs.server.controller.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.service.ai.AiProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 프로필", description = "사용자 AI 프로필 관련 API")
public class AiController {

    private final AiProfileService aiProfileService;

    @PostMapping("/profiles")
    @Operation(summary = "AI 프로필 생성")
    public ResponseEntity<AiProfileResponse> generateProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        AiProfile savedProfile = aiProfileService.generateCompleteUserProfile(userId);
        AiProfileResponse response = new AiProfileResponse(
                savedProfile.getPersonaDescription(),
                savedProfile.getInterestKeywords()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}