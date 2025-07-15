package kr.co.amateurs.server.service.ai;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThan;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final PostService postService;

    private static final Pattern IMG_TAG_PATTERN =
            Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE);


    /**
     * 게시글 임베딩을 생성하고 저장합니다.
     * @param post 게시글 정보
     */
    public void createPostEmbeddings(Post post) {
        try {
            String content = formatPostContent(post);

            long createdTimestamp = post.getCreatedAt()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toEpochSecond();

            Metadata metadata = Metadata.from(Map.of(
                    "userId", post.getUser().getId().toString(),
                    "postId", post.getId().toString(),
                    "title", post.getTitle(),
                    "boardType", post.getBoardType().name(),
                    "createdDate", createdTimestamp
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
     * 특정 게시글의 임베딩을 삭제합니다.
     * @param postId 게시글 ID
     */
    public void deletePostEmbedding(Long postId) {
        try {
            Filter postIdFilter = new IsEqualTo("postId", postId.toString());
            embeddingStore.removeAll(postIdFilter);
            log.info("게시글 임베딩 삭제 완료: postId={}", postId);
        } catch (Exception e) {
            log.error("게시글 임베딩 삭제 실패: postId={}", postId, e);
        }
    }

    /**
     * 특정 게시글의 임베딩을 업데이트합니다 (삭제 후 재생성).
     * @param post 게시글 정보
     */
    public void updatePostEmbedding(Post post) {
        try {
            deletePostEmbedding(post.getId());
            createPostEmbeddings(post);

            log.info("게시글 임베딩 업데이트 완료: postId={}", post.getId());
        } catch (Exception e) {
            log.error("게시글 임베딩 업데이트 실패: postId={}", post.getId(), e);
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

    /**
     * 게시글 내용을 포맷팅합니다.
     * 이미지 태그는 제거하고, 내용이 없을 경우 빈 문자열로 처리합니다.
     * @param post 게시글 정보
     * @return 포맷된 게시글 내용
     */
    private String formatPostContent(Post post) {
        String content = post.getContent() != null ? post.getContent() : "";
        content = IMG_TAG_PATTERN.matcher(content).replaceAll("");
        content = content.replaceAll("\\s+", " ").trim();
        return String.format("제목: %s\n내용: %s", post.getTitle(), content);
    }

    /**
     * N일 이전의 임베딩 데이터를 삭제합니다
     * @param daysBack 며칠 이전 데이터를 삭제할지
     */
    public void deleteOldEmbeddings(int daysBack) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
            long beforeTimestamp = cutoffDate
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toEpochSecond();

            Filter dateFilter = new IsLessThan("createdDate", beforeTimestamp);
            embeddingStore.removeAll(dateFilter);

            log.info("{}일 이전 임베딩 데이터 삭제 완료 (기준일: {})", daysBack, cutoffDate);

        } catch (Exception e) {
            log.error("{}일 이전 임베딩 데이터 삭제 실패", daysBack, e);
            throw new RuntimeException("임베딩 데이터 삭제 실패", e);
        }
    }
}
