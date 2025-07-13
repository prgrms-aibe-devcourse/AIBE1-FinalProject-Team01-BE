package kr.co.amateurs.server.controller.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;

import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.dto.post.PopularPostResponse;
import kr.co.amateurs.server.service.ai.AiProfileService;
import kr.co.amateurs.server.service.ai.PostEmbeddingManageService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.ai.PostRecommendService;
import kr.co.amateurs.server.service.post.PopularPostService;
import kr.co.amateurs.server.service.scheduler.AiPostRecommendScehdularService;
import kr.co.amateurs.server.service.scheduler.AiProfileSchedulerService;
import kr.co.amateurs.server.service.scheduler.PopularPostSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 프로필", description = "사용자 AI 프로필 관련 API")
public class AiController {

    private final PostRecommendService postRecommendService;
    private final PopularPostService popularPostService;

    @GetMapping("/posts/recommendations")
    @Operation(summary = "AI 개인화 게시글 조회", description = "사용자의 AI 프로필을 기반으로 맞춤 게시글을 추천합니다")
    public ResponseEntity<List<PostRecommendationResponse>> getAiRecommendations(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(name="limit", defaultValue="10") int limit) {

        Long userId = currentUser.getUser().getId();
        List<PostRecommendationResponse> recommendations = postRecommendService.getStoredRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/posts/popular")
    @Operation(summary="일반 인기글 조회", description= "인기 게시글을 조회합니다. (조회수 + 좋아요 + 댓글 기반)")
    public ResponseEntity<List<PopularPostResponse>> getPopularPosts(
            @RequestParam(name="limit", defaultValue="10") int limit) {
        StopWatch stopWatch = new StopWatch("PopularPosts API");
        stopWatch.start("getPopularPosts");
        List<PopularPostResponse> popularPosts = popularPostService.getPopularPosts(limit);
        stopWatch.stop();
        log.info("인기글 조회 완료 - 소요시간: {}ms, 결과 개수: {}",
                stopWatch.getLastTaskTimeMillis(), popularPosts.size());
        return ResponseEntity.ok(popularPosts);
    }

}