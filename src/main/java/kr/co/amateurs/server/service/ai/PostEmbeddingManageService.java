package kr.co.amateurs.server.service.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 게시글 임베딩 관리를 위한 유틸리티 서비스
 * CompletableFuture 비동기 처리를 공통화하여 코드 중복을 제거합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostEmbeddingManageService {

    private final PostEmbeddingService postEmbeddingService;
    private final PostService postService;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 모든 게시글의 임베딩을 초기화합니다 (전체 삭제 후 재생성)
     * @return 처리 결과 메시지
     */
    public void initializeAllPostEmbeddings() {
        try {
            embeddingStore.removeAll();
            List<Post> allPosts = postService.findAllPosts();

            for (Post post : allPosts) {
                postEmbeddingService.createPostEmbeddings(post);
            }
            log.info("임베딩 전체 재생성 완료 - 처리된 게시글: {}개", allPosts.size());

        } catch (Exception e) {
            log.error("임베딩 초기화 실패", e);
            throw ErrorCode.ERROR_AI_EMBEDDING_GENERATION.get();
        }
    }
}
