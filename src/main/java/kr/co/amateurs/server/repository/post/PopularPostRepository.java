package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.dto.post.PopularPostRequest;
import kr.co.amateurs.server.domain.dto.post.PopularPostResponse;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
                        POSTS.ID,
                        POST_STATISTICS.VIEW_COUNT,
                        POSTS.LIKE_COUNT,
                        DSL.count(COMMENTS.ID).as("commentCount"),
                        USERS.NICKNAME,
                        USERS.DEVCOURSE_NAME,
                        POSTS.CREATED_AT,
                        POSTS.TITLE,
                        POSTS.BOARD_TYPE
                )
                .from(POSTS)
                .join(USERS).on(USERS.ID.eq(POSTS.USER_ID))  // User JOIN 추가
                .join(POST_STATISTICS).on(POST_STATISTICS.POST_ID.eq(POSTS.ID))
                .leftJoin(COMMENTS).on(COMMENTS.POST_ID.eq(POSTS.ID))
                .where(POSTS.IS_DELETED.eq(false))
                .and(POSTS.IS_BLINDED.eq(false))
                .and(POSTS.CREATED_AT.ge(threeDaysAgo))
                .groupBy(POSTS.ID)
                .fetch()
                .map(record -> new PopularPostRequest(
                        record.get(POSTS.ID),
                        record.get(POST_STATISTICS.VIEW_COUNT),
                        record.get(POSTS.LIKE_COUNT),
                        record.get("commentCount", Integer.class),
                        null,
                        null,
                        record.get(USERS.NICKNAME),
                        record.get(USERS.DEVCOURSE_NAME) != null ?
                                DevCourseTrack.valueOf(record.get(USERS.DEVCOURSE_NAME)) : null,
                        record.get(POSTS.CREATED_AT),
                        record.get(POSTS.TITLE),
                        BoardType.valueOf(record.get(POSTS.BOARD_TYPE, String.class)),
                        null,
                        false,
                        false
                ));
    }

    public void savePopularPosts(List<PopularPostRequest> requests) {
        if (requests.isEmpty()) return;

        var rows = requests.stream()
                .map(request -> DSL.row(
                        request.postId(),
                        request.popularityScore(),
                        request.calculatedDate(),
                        request.boardType().name(),
                        request.boardId()
                ))
                .toList();

        dsl.insertInto(POPULAR_POSTS)
                .columns(
                        POPULAR_POSTS.POST_ID,
                        POPULAR_POSTS.POPULARITY_SCORE,
                        POPULAR_POSTS.CALCULATED_DATE,
                        POPULAR_POSTS.BOARD_TYPE,
                        POPULAR_POSTS.BOARD_ID
                )
                .valuesOfRows(rows)
                .execute();

        log.info("인기글 {}개 저장 완료", requests.size());
    }

    public List<PopularPostRequest> findLatestPopularPosts(int limit) {
        var latestDate = dsl.select(DSL.max(POPULAR_POSTS.CALCULATED_DATE))
                .from(POPULAR_POSTS)
                .fetchOneInto(LocalDate.class);

        if (latestDate == null) return List.of();

        return dsl.select(
                        POSTS.ID,
                        POST_STATISTICS.VIEW_COUNT,
                        POSTS.LIKE_COUNT,
                        DSL.count(COMMENTS.ID).as("commentCount"),
                        POPULAR_POSTS.POPULARITY_SCORE,
                        POPULAR_POSTS.CALCULATED_DATE,
                        USERS.NICKNAME,
                        USERS.DEVCOURSE_NAME,
                        POSTS.CREATED_AT,
                        POSTS.TITLE,
                        POPULAR_POSTS.BOARD_TYPE,
                        POPULAR_POSTS.BOARD_ID,
                        POSTS.IS_BLINDED,
                        POSTS.IS_DELETED
                )
                .from(POPULAR_POSTS)
                .join(POSTS).on(POSTS.ID.eq(POPULAR_POSTS.POST_ID))
                .join(USERS).on(USERS.ID.eq(POSTS.USER_ID))
                .join(POST_STATISTICS).on(POST_STATISTICS.POST_ID.eq(POSTS.ID))
                .leftJoin(COMMENTS).on(COMMENTS.POST_ID.eq(POSTS.ID))
                .where(POPULAR_POSTS.CALCULATED_DATE.eq(latestDate))
                .and(POSTS.IS_BLINDED.eq(false))
                .and(POSTS.IS_DELETED.eq(false))
                .groupBy(POSTS.ID, POPULAR_POSTS.POPULARITY_SCORE, POPULAR_POSTS.CALCULATED_DATE, 
                        POPULAR_POSTS.BOARD_TYPE, POPULAR_POSTS.BOARD_ID)
                .orderBy(POPULAR_POSTS.POPULARITY_SCORE.desc())
                .limit(limit)
                .fetch()
                .map(record -> new PopularPostRequest(
                        record.get(POSTS.ID),
                        record.get(POST_STATISTICS.VIEW_COUNT),
                        record.get(POSTS.LIKE_COUNT),
                        record.get("commentCount", Integer.class),
                        record.get(POPULAR_POSTS.POPULARITY_SCORE),
                        record.get(POPULAR_POSTS.CALCULATED_DATE),
                        record.get(USERS.NICKNAME),
                        record.get(USERS.DEVCOURSE_NAME) != null ?
                                DevCourseTrack.valueOf(record.get(USERS.DEVCOURSE_NAME)) : null,
                        record.get(POSTS.CREATED_AT),
                        record.get(POSTS.TITLE),
                        BoardType.valueOf(record.get(POPULAR_POSTS.BOARD_TYPE)),
                        record.get(POPULAR_POSTS.BOARD_ID),
                        record.get(POSTS.IS_BLINDED),
                        record.get(POSTS.IS_DELETED)
                ));
    }

    public boolean existsByDate(LocalDate date) {
        return dsl.fetchExists(
                dsl.selectFrom(POPULAR_POSTS)
                        .where(POPULAR_POSTS.CALCULATED_DATE.eq(date))
        );
    }

    public void deleteBeforeDate(LocalDate date) {
        int count = dsl.deleteFrom(POPULAR_POSTS)
                .where(POPULAR_POSTS.CALCULATED_DATE.lt(date))
                .execute();
        log.info("날짜 {} 인기글 {}개 삭제", date, count);
    }

    public BoardStatus getBoardStatus(Long postId) {
        var record = dsl.select(POSTS.IS_BLINDED, POSTS.IS_DELETED)
                .from(POSTS)
                .where(POSTS.ID.eq(postId))
                .fetchOne();

        return record != null ?
                new BoardStatus(record.get(POSTS.IS_BLINDED), record.get(POSTS.IS_DELETED)) :
                new BoardStatus(false, false);
    }

    public record BoardStatus(boolean isBlinded, boolean isDeleted) {}

}
