package kr.co.amateurs.server.repository.user;

import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import kr.co.amateurs.server.domain.dto.user.UserModalInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import static org.jooq.generated.Tables.*;
import static org.jooq.generated.Tables.FOLLOWS;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserJooqRepository {
    private final DSLContext dslContext;

    public UserModalInfoResponseDTO findUserModalInfoByNickname(String nickname, Long currentUserId) {
        var followerCountSubquery = DSL.select(DSL.count())
                .from(FOLLOWS.as("f_followers"))
                .where(FOLLOWS.as("f_followers").TO_USER_ID.eq(USERS.ID));

        var followingCountSubquery = DSL.select(DSL.count())
                .from(FOLLOWS.as("f_following"))
                .where(FOLLOWS.as("f_following").FROM_USER_ID.eq(USERS.ID));

        var postCountSubquery = DSL.select(DSL.count())
                .from(POSTS)
                .where(POSTS.USER_ID.eq(USERS.ID));

        var isFollowingCondition = DSL.exists(
                DSL.selectOne()
                        .from(FOLLOWS.as("f_check"))
                        .where(FOLLOWS.as("f_check").FROM_USER_ID.eq(currentUserId)
                                .and(FOLLOWS.as("f_check").TO_USER_ID.eq(USERS.ID)))
        );

        return dslContext.select(
                        USERS.ID.as("userId"),
                        USERS.NICKNAME,
                        USERS.IMAGE_URL.as("profileImg"),
                        USERS.DEVCOURSE_NAME.as("devcourseTrack"),
                        USERS.DEVCOURSE_BATCH.as("devcourseBatch"),
                        postCountSubquery.asField("postCount"),
                        followerCountSubquery.asField("follwerCount"),
                        followingCountSubquery.asField("followingCount"),
                        DSL.case_()
                                .when(isFollowingCondition, true)
                                .otherwise(false)
                                .as("isFollowing")
                )
                .from(USERS)
                .where(USERS.NICKNAME.eq(nickname))
                .fetchOneInto(UserModalInfoResponseDTO.class);
    }
}
