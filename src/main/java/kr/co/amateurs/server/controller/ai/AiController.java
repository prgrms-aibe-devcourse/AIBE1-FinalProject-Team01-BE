package kr.co.amateurs.server.controller.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.service.ai.AiProfileService;
import kr.co.amateurs.server.service.ai.PostEmbeddingManageService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.ai.PostRecommendService;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 프로필", description = "사용자 AI 프로필 관련 API")
public class AiController {

    private final AiProfileService aiProfileService;
    private final PostRecommendService postRecommendService;
    private final PostEmbeddingManageService postEmbeddingManageService;
    private final PostService postService;

    @PostMapping("/profiles")
    @Operation(summary = "AI 프로필 생성")
    public ResponseEntity<AiProfileResponse> generateProfile(@AuthenticationPrincipal  CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        AiProfileResponse response = aiProfileService.generateUserProfileResponse(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/recommendations")
    @Operation(summary = "AI 개인화 게시글 추천", description = "사용자의 AI 프로필을 기반으로 맞춤 게시글을 추천합니다")
    public ResponseEntity<List<Post>> getAiRecommendations(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = currentUser.getUser().getId();
        List<Post> recommendations = postRecommendService.getAiPersonalRecommendedPosts(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/posts/popular")
    @Operation(summary="일반 인기글 추천", description= "인기 게시글을 추천합니다. (좋아요 기반)")
    public ResponseEntity<List<Post>> getPopularPosts(
            @RequestParam(defaultValue = "10") int limit) {

        List<Post> popularPosts = postService.findPopularPosts(limit);
        return ResponseEntity.ok(popularPosts);
    }

    @PostMapping("/embeddings/initialize")
    @Operation(summary = "[개발용] 게시글 임베딩 초기화", description = "모든 게시글의 임베딩을 생성하고 저장합니다")
    public ResponseEntity<Void> initializeAllEmbeddings() {
        postEmbeddingManageService.initializeAllPostEmbeddings();
        return ResponseEntity.ok().build();
    }
}