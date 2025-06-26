package kr.co.amateurs.server.service.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    /**
     * AI 프로필 기반 게시글 추천
     * @param userId 사용자 ID
     * @param limit 추천 게시글 개수
     * @return 추천 게시글 목록
     */
    public List<Post> getAiPersonalRecommendedPosts(Long userId, int limit) {
        try {
            log.info("추천 시작: userId={}, limit={}", userId, limit);
            Optional<AiProfile> aiProfile = aiProfileRepository.findByUserId(userId);

            if(aiProfile.isPresent()) {
                log.info("AI 프로필 조회 성공 / 게시글 추천 시작 : userId={}", userId);
                return getAiRecommendPosts(aiProfile.get(), userId, limit);
            } else {
                log.info("AI 프로필 없음 - 인기글 추천: userId={}", userId);
                return getGuestPost(userId, limit);
            }
        } catch(Exception e) {
            log.error("추천 오류 - 인기글 추천 : userId={}, error={}", userId, e.getMessage());
            return getGuestPost(userId, limit);
        }
    }

    /**
     * AI 프로필 기반 게시글 추천
     * @param aiProfile AI 프로필
     * @param userId 사용자 ID
     * @param limit 추천 게시글 개수
     * @return 추천 게시글 목록
     */
    private List<Post> getAiRecommendPosts(AiProfile aiProfile, Long userId, int limit) {
        try {
            log.info("AI 프로필 기반 게시글 추천 시작: userId={}, limit={}", userId, limit);
            String query = buildRecommendationQuery(aiProfile);
            List<EmbeddingMatch<TextSegment>> matches = postEmbeddingService.findSimilarPosts(query, limit);
            List<Post> recommendedPosts = matches.stream()
                    .filter(match -> match.embedded().metadata().containsKey("postId"))
                    .filter(match -> match.embedded().metadata().containsKey("userId"))
                    .filter(match -> !match.embedded().metadata().getString("userId").equals(userId.toString()))
                    .map(match -> Long.parseLong(match.embedded().metadata().getString("postId")))
                    .distinct()
                    .limit(limit)
                    .map(postService::findById)
                    .collect(Collectors.toList());

            log.info("AI 기반 게시글 추천 완료: userId={}, 추천 게시글 수={}", userId, recommendedPosts.size());
            return recommendedPosts;
        } catch (Exception e) {
            log.error("AI 프로필 기반 게시글 추천 실패: userId={}, error={}", userId, e.getMessage());
            return getGuestPost(userId, limit);
        }
    }

    private List<Post> getGuestPost(Long userId, int limit) {
        return postService.findPopularPosts(limit);
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
