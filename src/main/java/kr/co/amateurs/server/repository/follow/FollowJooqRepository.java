package kr.co.amateurs.server.repository.follow;

import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static org.jooq.generated.Tables.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FollowJooqRepository {

    private final DSLContext dslContext;

    /**
     * 특정 사용자가 팔로우하는 사용자 목록 조회
     * @param userId 조회할 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 팔로잉 목록
     */
    public Page<FollowResponseDTO> findFollowingList(Long userId, Pageable pageable) {
        var followerCountSubquery = DSL.select(DSL.count())
                .from(FOLLOWS.as("f2"))
                .where(FOLLOWS.as("f2").TO_USER_ID.eq(USERS.ID));

        var followingCountSubquery = DSL.select(DSL.count())
                .from(FOLLOWS.as("f3"))
                .where(FOLLOWS.as("f3").FROM_USER_ID.eq(USERS.ID));

        var postCountSubquery = DSL.select(DSL.count())
                .from(POSTS)
                .where(POSTS.USER_ID.eq(USERS.ID));

        var results = dslContext.select(
                        USERS.ID.as("userId"),
                        USERS.NICKNAME,
                        USERS.IMAGE_URL.as("profileImg"),
                        USERS.DEVCOURSE_NAME.as("devcourseTrack"),
                        USERS.DEVCOURSE_BATCH.as("devcourseBatch"),
                        postCountSubquery.asField("postCount"),
                        followerCountSubquery.asField("follwerCount"), // DTO의 오타된 필드명에 맞춤
                        followingCountSubquery.asField("followingCount")
                )
                .from(FOLLOWS)
                .join(USERS).on(FOLLOWS.TO_USER_ID.eq(USERS.ID))
                .where(FOLLOWS.FROM_USER_ID.eq(userId))
                .orderBy(FOLLOWS.CREATED_AT.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(FollowResponseDTO.class);

        Integer totalCount = dslContext.selectCount()
                .from(FOLLOWS)
                .where(FOLLOWS.FROM_USER_ID.eq(userId))
                .fetchOptional(0, Integer.class)
                .orElse(0);

        return new PageImpl<>(results, pageable, totalCount);
    }
}