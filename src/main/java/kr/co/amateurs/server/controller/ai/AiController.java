package kr.co.amateurs.server.controller.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.service.ai.AiProfileService;
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
    private final PostEmbeddingService postEmbeddingService;
    private final PostService postService;

    @PostMapping("/profiles")
    @Operation(summary = "AI 프로필 생성")
    public ResponseEntity<AiProfileResponse> generateProfile(@AuthenticationPrincipal  CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        AiProfile savedProfile = aiProfileService.generateCompleteUserProfile(userId);
        AiProfileResponse response = new AiProfileResponse(
                savedProfile.getPersonaDescription(),
                savedProfile.getInterestKeywords()
        );
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
    @Operation(summary = "일반 인기글 추천", description = "일주일 이내 좋아요가 많은 인기 게시글을 추천합니다")
    public ResponseEntity<List<Post>> getPopularPosts(
            @RequestParam(defaultValue = "10") int limit) {

        List<Post> popularPosts = postService.findPopularPosts(limit);
        return ResponseEntity.ok(popularPosts);
    }

    @PostMapping("/embeddings/posts/{postId}")
    @Operation(summary = "특정 게시글 임베딩 생성", description = "개발/테스트용 - 특정 게시글의 임베딩을 생성합니다")
    public ResponseEntity<String> createPostEmbedding(@PathVariable Long postId) {
        try {
            Post post = postService.findById(postId);
            postEmbeddingService.createPostEmbeddings(post);
            return ResponseEntity.ok("게시글 임베딩이 생성되었습니다: " + postId);
        } catch (Exception e) {
            log.error("게시글 임베딩 생성 실패: postId={}", postId, e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    @PostMapping("/embeddings/initialize")
    @Operation(summary = "모든 게시글 임베딩 초기화", description = "개발/테스트용 - 모든 기존 게시글의 임베딩을 생성합니다")
    public ResponseEntity<String> initializeAllEmbeddings() {
        try {
            List<Post> allPosts = postService.findRecentPosts(100);

            for (Post post : allPosts) {
                postEmbeddingService.createPostEmbeddings(post);
            }

            return ResponseEntity.ok("임베딩 초기화 완료: " + allPosts.size() + "개 게시글");
        } catch (Exception e) {
            log.error("임베딩 초기화 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }
}