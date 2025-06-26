package kr.co.amateurs.server.service.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEmbeddingManageService {

    private final PostEmbeddingService postEmbeddingService;
    private final PostService postService;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 전체 게시글의 임베딩을 초기화합니다.
     * 이미 임베딩된 게시글은 스킵하고, 임베딩이 없는 게시글만 처리합니다.
     * @return 처리 결과 메시지
     */
    public String initializeAllPostEmbeddings() {
        try {
            List<Post> allPosts = postService.findAllPosts();

            // 이미 임베딩된 postId들 수집
            Set<Long> existingPostIds = getExistingEmbeddingPostIds();

            // 임베딩이 없는 게시글만 필터링
            List<Post> postsToProcess = allPosts.stream()
                    .filter(post -> !existingPostIds.contains(post.getId()))
                    .toList();

            // 임베딩 생성
            for (Post post : postsToProcess) {
                postEmbeddingService.createPostEmbeddings(post);
            }

            String resultMessage = String.format(
                    "임베딩 초기화 완료 - 전체: %d개, 처리: %d개, 스킵: %d개",
                    allPosts.size(), postsToProcess.size(), allPosts.size() - postsToProcess.size()
            );
            log.info(resultMessage);
            return resultMessage;

        } catch (Exception e) {
            log.error("임베딩 초기화 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    /**
     * 특정 게시글의 임베딩을 생성합니다.
     * @return
     */
    public String clearAllEmbeddings() {
        try {
            embeddingStore.removeAll();
            String resultMessage = "모든 임베딩이 삭제되었습니다";
            log.info(resultMessage);
            return resultMessage;

        } catch (Exception e) {
            log.error("임베딩 삭제 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    /**
     * 기존 임베딩된 게시글의 postId를 조회합니다.
     * @return
     */
    private Set<Long> getExistingEmbeddingPostIds() {
        try {
            List<EmbeddingMatch<TextSegment>> allEmbeddings = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(embeddingModel.embed("").content())
                            .maxResults(10000)
                            .minScore(0.0)
                            .build()
            ).matches();
            return allEmbeddings.stream()
                    .map(match -> Long.parseLong(match.embedded().metadata().getString("postId")))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("기존 임베딩 postId 조회 실패", e);
            return Set.of();
        }
    }
}