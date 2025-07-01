package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.dto.post.PopularPostRequest;
import kr.co.amateurs.server.domain.entity.post.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.generated.Tables.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PopularPostRepository {
    private final DSLContext dsl;

    public List<PopularPostRequest> findRecentPostsWithCounts(LocalDateTime threeDaysAgo) {
        return dsl.select(
                        POSTS.ID.as("postId"),
                        POSTS.VIEW_COUNT.as("viewCount"),
                        POSTS.LIKE_COUNT.as("likeCount"),
                        DSL.count(COMMENTS.ID).as("commentCount")
                )
                .from(POSTS)
                .leftJoin(COMMENTS).on(COMMENTS.POST_ID.eq(POSTS.ID))
                .where(POSTS.IS_DELETED.eq(false))
                .and(POSTS.CREATED_AT.ge(threeDaysAgo))
                .groupBy(POSTS.ID, POSTS.VIEW_COUNT, POSTS.LIKE_COUNT)
                .fetchInto(PopularPostRequest.class);
    }

    public void savePopularPosts(List<PopularPostRequest> requests) {
        if (requests.isEmpty()) return;

        var batch = dsl.batch(
                dsl.insertInto(POPULAR_POSTS)
                        .columns(
                                POPULAR_POSTS.POST_ID,
                                POPULAR_POSTS.POPULARITY_SCORE,
                                POPULAR_POSTS.CALCULATED_DATE,
                                POPULAR_POSTS.VIEW_COUNT,
                                POPULAR_POSTS.LIKE_COUNT,
                                POPULAR_POSTS.COMMENT_COUNT
                        )
                        .values((Long) null, null, null, null, null, null)
        );

        for (PopularPostRequest request : requests) {
            batch.bind(
                    request.postId(),
                    request.popularityScore(),
                    request.calculatedDate(),
                    request.viewCount(),
                    request.likeCount(),
                    request.commentCount()
            );
        }

        batch.execute();
        log.info("인기글 {}개 저장 완료", requests.size());
    }

    public void deleteBeforeDate(LocalDate date) {
        int count = dsl.deleteFrom(POPULAR_POSTS)
                .where(POPULAR_POSTS.CALCULATED_DATE.lt(date))
                .execute();
        log.info("날짜 {} 인기글 {}개 삭제", date, count);
    }

    public List<Post> findLatestPopularPosts(int limit) {
        var latestDate = dsl.select(DSL.max(POPULAR_POSTS.CALCULATED_DATE))
                .from(POPULAR_POSTS)
                .fetchOneInto(LocalDate.class);

        if (latestDate == null) return List.of();

        var postIds = dsl.select(POPULAR_POSTS.POST_ID)
                .from(POPULAR_POSTS)
                .where(POPULAR_POSTS.CALCULATED_DATE.eq(latestDate))
                .orderBy(POPULAR_POSTS.POPULARITY_SCORE.desc())
                .limit(limit)
                .fetch(POPULAR_POSTS.POST_ID);

        if (postIds.isEmpty()) return List.of();

        var postsMap = dsl.selectFrom(POSTS)
                .where(POSTS.ID.in(postIds))
                .and(POSTS.IS_DELETED.eq(false))
                .fetchMap(POSTS.ID, Post.class);

        return postIds.stream()
                .map(postsMap::get)
                .filter(post -> post != null)
                .toList();
    }

    public boolean existsByDate(LocalDate date) {
        return dsl.fetchExists(
                dsl.selectFrom(POPULAR_POSTS)
                        .where(POPULAR_POSTS.CALCULATED_DATE.eq(date))
        );
    }
}
