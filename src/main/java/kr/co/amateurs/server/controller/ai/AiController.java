package kr.co.amateurs.server.controller.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.dto.post.PopularPostResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.service.ai.AiProfileService;
import kr.co.amateurs.server.service.ai.PostEmbeddingManageService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.ai.PostRecommendService;
import kr.co.amateurs.server.service.post.PopularPostService;
import kr.co.amateurs.server.service.post.PostService;
import kr.co.amateurs.server.service.scheduler.AiPostRecommendScehdularService;
import kr.co.amateurs.server.service.scheduler.AiProfileSchedulerService;
import kr.co.amateurs.server.service.scheduler.PopularPostSchedulerService;
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
    private final PostEmbeddingService postEmbeddingService;
    private final PopularPostService popularPostService;
    private final AiProfileSchedulerService aiProfileSchedulerService;
    private final PopularPostSchedulerService popularPostSchedulerService;
    private final AiPostRecommendScehdularService aiPostRecommendScehdularService;

    @GetMapping("/posts/recommendations")
    @Operation(summary = "AI 개인화 게시글 조회", description = "사용자의 AI 프로필을 기반으로 맞춤 게시글을 추천합니다")
    public ResponseEntity<List<PostRecommendationResponse>> getAiRecommendations(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = currentUser.getUser().getId();
        List<PostRecommendationResponse> recommendations = postRecommendService.getStoredRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/posts/popular")
    @Operation(summary="일반 인기글 조회", description= "인기 게시글을 조회합니다. (조회수 + 좋아요 + 댓글 기반)")
    public ResponseEntity<List<PopularPostResponse>> getPopularPosts(
            @RequestParam(defaultValue = "10") int limit) {

        List<PopularPostResponse> popularPosts = popularPostService.getPopularPosts(limit);
        return ResponseEntity.ok(popularPosts);
    }

}