package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.post.PostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.generated.Tables.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostJooqRepository {

    private final DSLContext dslContext;

    public Page<PostResponseDTO> findPostsByType(Long userId, Pageable pageable, String type) {
        var selectQuery = dslContext.select(
                        DSL.case_(POSTS.BOARD_TYPE)
                                .when("FREE", COMMUNITY_POSTS.ID)
                                .when("QNA", COMMUNITY_POSTS.ID)
                                .when("RETROSPECT", COMMUNITY_POSTS.ID)
                                .when("REVIEW", IT_POSTS.ID)
                                .when("NEWS", IT_POSTS.ID)
                                .when("PROJECT_HUB", PROJECTS.ID)
                                .when("GATHER", GATHERING_POSTS.ID)
                                .when("MATCH", MATCHING_POSTS.ID)
                                .when("MARKET", MARKET_ITEMS.ID)
                                .as("id"),
                        POSTS.ID.as("postId"),
                        POSTS.IS_BLINDED,
                        POSTS.BOARD_TYPE,
                        POSTS.TITLE,
                        USERS.NICKNAME,
                        USERS.IMAGE_URL,
                        USERS.DEVCOURSE_NAME.as("devCourseTrack"),
                        POSTS.LIKE_COUNT,
                        POST_STATISTICS.VIEW_COUNT,
                        DSL.count(COMMENTS.ID).as("commentCount"),
                        POSTS.TAG,
                        POSTS.CREATED_AT,
                        POSTS.UPDATED_AT
                )
                .from(POSTS)
                .join(USERS).on(POSTS.USER_ID.eq(USERS.ID))
                .join(POST_STATISTICS).on(POSTS.ID.eq(POST_STATISTICS.POST_ID))
                .leftJoin(COMMUNITY_POSTS).on(POSTS.ID.eq(COMMUNITY_POSTS.POST_ID))
                .leftJoin(IT_POSTS).on(POSTS.ID.eq(IT_POSTS.POST_ID))
                .leftJoin(PROJECTS).on(POSTS.ID.eq(PROJECTS.POST_ID))
                .leftJoin(GATHERING_POSTS).on(POSTS.ID.eq(GATHERING_POSTS.POST_ID))
                .leftJoin(MATCHING_POSTS).on(POSTS.ID.eq(MATCHING_POSTS.POST_ID))
                .leftJoin(MARKET_ITEMS).on(POSTS.ID.eq(MARKET_ITEMS.POST_ID))
                .leftJoin(COMMENTS).on(COMMENTS.POST_ID.eq(POSTS.ID));



        var results = switch (type) {
            case "my" -> selectQuery
                    .where(POSTS.USER_ID.eq(userId))
                    .groupBy(POSTS.ID)
                    .orderBy(POSTS.CREATED_AT.desc())
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetchInto(PostResponseDTO.class);

            case "bookmarked" -> selectQuery
                    .where(POSTS.ID.in(
                            dslContext.select(BOOKMARKS.POST_ID)
                                    .from(BOOKMARKS)
                                    .where(BOOKMARKS.USER_ID.eq(userId))
                    ))
                    .groupBy(POSTS.ID)
                    .orderBy(POSTS.CREATED_AT.desc())
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetchInto(PostResponseDTO.class);

            case "liked" -> selectQuery
                    .where(POSTS.ID.in(
                            dslContext.select(POST_LIKE.POST_ID)
                                    .from(POST_LIKE)
                                    .where(POST_LIKE.USER_ID.eq(userId))
                    ))
                    .groupBy(POSTS.ID)
                    .orderBy(POSTS.CREATED_AT.desc())
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetchInto(PostResponseDTO.class);

            default -> throw ErrorCode.NOT_FOUND.get();
        };

        int total = getTotalCount(userId, type);

        return new PageImpl<>(results, pageable, total);
    }

    private int getTotalCount(Long userId, String type) {
        return switch (type) {
            case "my" -> dslContext.selectCount()
                    .from(POSTS)
                    .where(POSTS.USER_ID.eq(userId))
                    .fetchOne(0, int.class);

            case "bookmarked" -> dslContext.selectCount()
                    .from(POSTS)
                    .where(POSTS.ID.in(
                            dslContext.select(BOOKMARKS.POST_ID)
                                    .from(BOOKMARKS)
                                    .where(BOOKMARKS.USER_ID.eq(userId))
                    ))
                    .fetchOne(0, int.class);

            case "liked" -> dslContext.selectCount()
                    .from(POSTS)
                    .where(POSTS.ID.in(
                            dslContext.select(POST_LIKE.POST_ID)
                                    .from(POST_LIKE)
                                    .where(POST_LIKE.USER_ID.eq(userId))
                    ))
                    .fetchOne(0, int.class);

            default -> 0;
        };
    }


    /**
     * BoardType별 실제 Board ID 조회
     */
    public Long getBoardId(Long postId, BoardType boardType) {
        return switch (boardType) {
            case MARKET -> dslContext.select(MARKET_ITEMS.ID)
                    .from(MARKET_ITEMS)
                    .where(MARKET_ITEMS.POST_ID.eq(postId))
                    .fetchOneInto(Long.class);

            case MATCH -> dslContext.select(MATCHING_POSTS.ID)
                    .from(MATCHING_POSTS)
                    .where(MATCHING_POSTS.POST_ID.eq(postId))
                    .fetchOneInto(Long.class);

            case GATHER -> dslContext.select(GATHERING_POSTS.ID)
                    .from(GATHERING_POSTS)
                    .where(GATHERING_POSTS.POST_ID.eq(postId))
                    .fetchOneInto(Long.class);

            case PROJECT_HUB -> dslContext.select(PROJECTS.ID)
                    .from(PROJECTS)
                    .where(PROJECTS.POST_ID.eq(postId))
                    .fetchOneInto(Long.class);

            case NEWS -> dslContext.select(IT_POSTS.ID)
                    .from(IT_POSTS)
                    .where(IT_POSTS.POST_ID.eq(postId))
                    .fetchOneInto(Long.class);

            case FREE, QNA, RETROSPECT, INFO, REVIEW -> dslContext.select(COMMUNITY_POSTS.ID)
                    .from(COMMUNITY_POSTS)
                    .where(COMMUNITY_POSTS.POST_ID.eq(postId))
                    .fetchOneInto(Long.class);

            default -> {
                log.warn("지원하지 않는 BoardType: {}", boardType);
                yield postId;
            }
        };
    }

}
