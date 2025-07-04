package kr.co.amateurs.server.controller.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.service.ai.AiProfileService;
import kr.co.amateurs.server.service.ai.PostEmbeddingManageService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.ai.PostRecommendService;
import kr.co.amateurs.server.service.scheduler.AiPostRecommendScehdularService;
import kr.co.amateurs.server.service.scheduler.AiProfileSchedulerService;
import kr.co.amateurs.server.service.scheduler.EmbeddingCleanupSchedulerService;
import kr.co.amateurs.server.service.scheduler.PopularPostSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/dev")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 개발", description = "AI 관련 개발용 컨트롤러")
@Profile({"local", "dev"})
public class AiDevController {

    private final AiProfileService aiProfileService;
    private final PostRecommendService postRecommendService;
    private final PostEmbeddingManageService postEmbeddingManageService;
    private final PostEmbeddingService postEmbeddingService;
    private final AiProfileSchedulerService aiProfileSchedulerService;
    private final PopularPostSchedulerService popularPostSchedulerService;
    private final AiPostRecommendScehdularService aiPostRecommendScehdularService;
    private final EmbeddingCleanupSchedulerService embeddingCleanupSchedulerService;


    @PostMapping("/profiles/initial")
    @Operation(summary = "초기 AI 프로필 생성", description = "토픽 기반 초기 프로필 생성 (가입 시 로직)")
    public ResponseEntity<AiProfile> generateInitialProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        AiProfile profile = aiProfileService.generateInitialProfile(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @PostMapping("/profiles")
    @Operation(summary = "AI 프로필 생성", description = "활동 기반 완전한 AI 프로필 생성")
    public ResponseEntity<AiProfileResponse> generateProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        AiProfileResponse response = aiProfileService.generateUserProfileResponse(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/scheduler/ai-profiles")
    @Operation(summary = "전체 활성 사용자 AI 프로필 업데이트", description = "스케줄러와 동일한 로직 실행")
    public ResponseEntity<Void> updateAllActiveUsersProfiles() {
        aiProfileSchedulerService.updateActiveUsersProfiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/scheduler/popular-posts")
    @Operation(summary = "인기글 계산 및 저장", description = "스케줄러와 동일한 로직 실행")
    public ResponseEntity<Void> calculateAndSavePopularPosts() {
        popularPostSchedulerService.updatePopularPosts();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/scheduler/recommendations")
    @Operation(summary = "전체 사용자 맞춤 추천 게시글 생성", description = "스케줄러와 동일한 로직 실행")
    public ResponseEntity<Void> updateAllUsersRecommendations() {
        aiPostRecommendScehdularService.updateAllUsersRecommendations();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/scheduler/embeddings/cleanup")
    @Operation(summary = "오래된 임베딩 데이터 정리", description = "스케줄러와 동일한 로직 실행 (7일 이전 데이터 삭제)")
    public ResponseEntity<Void> cleanupOldEmbeddings() {
        embeddingCleanupSchedulerService.cleanupOldEmbeddings();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recommendations/save")
    @Operation(summary = "특정 사용자 추천 게시글 생성", description = "개별 사용자 추천 생성 및 저장")
    public ResponseEntity<Void> saveUserRecommendations(
            @RequestParam Long userId,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        postRecommendService.saveRecommendationsToDB(userId, limit);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recommendations")
    @Operation(summary = "특정 사용자 추천 게시글 조회", description = "저장된 추천 게시글을 조회합니다 (개발용)")
    public ResponseEntity<List<PostRecommendationResponse>> getUserRecommendations(
            @RequestParam Long userId,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        StopWatch stopWatch = new StopWatch("추천 게시글 조회");
        stopWatch.start();

        List<PostRecommendationResponse> recommendations = postRecommendService.getStoredRecommendations(userId, limit);

        stopWatch.stop();
        log.info("추천 게시글 조회 완료: userId={}, limit={}, 소요시간={}ms",
                userId, limit, stopWatch.getTotalTimeMillis());
        return ResponseEntity.ok(recommendations);
    }


    @PostMapping("/embeddings/post/{postId}")
    @Operation(summary = "특정 게시글 임베딩 생성", description = "특정 게시글의 임베딩을 생성합니다")
    public ResponseEntity<String> createSpecificPostEmbedding(@PathVariable Long postId) {
        String result = postEmbeddingService.createSpecificPostEmbedding(postId);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/embeddings/initialize")
    @Operation(summary = "게시글 임베딩 초기화", description = "모든 게시글의 임베딩을 생성하고 저장합니다")
    public ResponseEntity<Void> initializeAllEmbeddings() {
        postEmbeddingManageService.initializeAllPostEmbeddings();
        return ResponseEntity.ok().build();
    }
}