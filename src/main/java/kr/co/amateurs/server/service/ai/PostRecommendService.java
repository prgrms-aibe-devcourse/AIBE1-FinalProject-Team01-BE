package kr.co.amateurs.server.service.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.dto.post.PopularPostResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.repository.ai.AiRecommendPostRepository;
import kr.co.amateurs.server.repository.post.PopularPostRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.post.PopularPostService;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostRecommendService {
    private final PostEmbeddingService postEmbeddingService;
    private final PostService postService;
    private final AiProfileRepository aiProfileRepository;
    private final UserService userService;
    private final AiRecommendPostRepository aiRecommendPostRepository;
    private final PopularPostService popularPostService;


    /**
     * AI 프로필 기반 게시글 추천 - (게시글 추천)
     * - AI 프로필이 있으면: AI 기반 추천
     * - AI 프로필이 없으면: 인기글 추천
     * - 오류 발생시: 인기글 fallback
     * @param userId 사용자 ID
     * @param limit 추천 게시글 개수
     * @return 추천 게시글 목록
     */
    public List<Post> getAiPersonalRecommendedPosts(Long userId, int limit) {
        log.info("AI 추천 시작: userId={}, limit={}", userId, limit);
        Optional<AiProfile> aiProfile = aiProfileRepository.findByUserId(userId);

        if (aiProfile.isEmpty()) {
            log.warn("AI 프로필 없음 - 추천 스킵: userId={}", userId);
            return Collections.emptyList();
        }

        // AI 프로필 기반 추천 로직
        log.info("AI 프로필 기반 게시글 추천 시작: userId={}", userId);
        String query = buildRecommendationQuery(aiProfile.get());
        List<EmbeddingMatch<TextSegment>> matches = postEmbeddingService.findSimilarPosts(query, limit);
        List<Post> recommendedPosts = matches.stream()
                .filter(match -> match.embedded().metadata().containsKey("postId"))
                .filter(match -> match.embedded().metadata().containsKey("userId"))
                .filter(match -> !match.embedded().metadata().getString("userId").equals(userId.toString()))
                .peek(match -> {
                    String postId = match.embedded().metadata().getString("postId");
                    double similarity = match.score();
                    log.info("추천 게시글: postId={}, 유사도={}", postId, similarity);
                })
                .map(match -> Long.parseLong(match.embedded().metadata().getString("postId")))
                .distinct()
                .limit(limit)
                .map(postService::findById)
                .collect(Collectors.toList());

        log.info("AI 기반 게시글 추천 완료: userId={}, 추천 게시글 수={}", userId, recommendedPosts.size());
        return recommendedPosts;

    }

    /**
     * 추천 게시글을 DB에 저장 - (추천 게시글 저장)
     * @param userId 사용자 ID
     * @param limit 추천 게시글 개수
     */
    @Transactional
    public void saveRecommendationsToDB(Long userId, int limit) {
        try {
            log.info("추천 게시글 생성 및 저장 시작: userId={}, limit={}", userId, limit);

            List<Post> recommendedPosts = getAiPersonalRecommendedPosts(userId, limit);

            aiRecommendPostRepository.deleteByUserId(userId);
            log.info("기존 추천 게시글 삭제 완료: userId={}", userId);

            User user = userService.findById(userId);
            for (Post post : recommendedPosts) {
                RecommendedPost recommendedPost = RecommendedPost.builder()
                        .user(user)
                        .post(post)
                        .build();
                aiRecommendPostRepository.save(recommendedPost);
            }
            log.info("추천 게시글 저장 완료: userId={}, 개수={}", userId, recommendedPosts.size());

        } catch (Exception e) {
            log.error("추천 게시글 저장 실패: userId={}", userId, e);
        }
    }

    /**
     * 메인 페이지에서 저장된 추천 게시글 조회
     * @param userId 사용자 ID
     * @param limit 조회할 게시글 개수
     * @return 추천 게시글 목록
     */
    public List<PostRecommendationResponse> getStoredRecommendations(Long userId, int limit) {
        List<RecommendedPost> recommendedPosts = aiRecommendPostRepository
                .findByUserIdOrderById(userId);

        return recommendedPosts.stream()
                .limit(limit)
                .map(rp -> PostRecommendationResponse.from(rp.getPost()))
                .collect(Collectors.toList());
    }

    private String buildRecommendationQuery(AiProfile profile) {
        StringBuilder query = new StringBuilder();
        if (profile.getPersonaDescription() != null) {
            query.append(profile.getPersonaDescription()).append(" ");
        }
        if (profile.getInterestKeywords() != null) {
            query.append(profile.getInterestKeywords());
        }
        return query.toString().trim();
    }
}
