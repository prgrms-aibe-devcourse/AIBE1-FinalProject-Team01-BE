package kr.co.amateurs.server.service.ai;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final PostService postService;

    /**
     * 게시글 임베딩을 생성하고 저장합니다.
     * @param post 게시글 정보
     */
    public void createPostEmbeddings(Post post) {
        try {
            String content = formatPostContent(post);

            Metadata metadata = Metadata.from(Map.of(
                    "userId", post.getUser().getId().toString(),
                    "postId", post.getId().toString(),
                    "title", post.getTitle(),
                    "boardType", post.getBoardType().name()
            ));

            TextSegment segment = TextSegment.from(content, metadata);

            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);

            log.info("게시글 임베딩 저장 완료: postId={}", post.getId());

        } catch (Exception e) {
            log.error("게시글 임베딩 생성 실패: postId={}", post.getId(), e);
        }
    }

    /**
     * [개발용] 특정 게시글의 임베딩을 생성합니다.
     * @param postId 게시글 ID
     * @return 생성 결과 메시지
     */
    public String createSpecificPostEmbedding(Long postId) {
        try {
            Post post = postService.findById(postId);
            createPostEmbeddings(post);
            return "게시글 임베딩이 생성되었습니다: " + postId;
        } catch (Exception e) {
            log.error("게시글 임베딩 생성 실패: postId={}", postId, e);
            throw ErrorCode.ERROR_AI_EMBEDDING_GENERATION.get();
        }
    }

    /**
     * 게시글 임베딩을 기반으로 유사한 게시글을 검색합니다.
     * @param query 검색어
     * @param limit 최대 검색 결과 개수
     * @return 유사한 게시글 목록
     */
    public List<EmbeddingMatch<TextSegment>> findSimilarPosts(String query, int limit) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(limit)
                .build();

        return embeddingStore.
                search(request).matches();
    }

    private String formatPostContent(Post post) {
        return String.format("제목: %s\n내용: %s",
                post.getTitle(),
                post.getContent() != null ? post.getContent() : ""
        );
    }
}
