package kr.co.amateurs.server.repository.project.strategy;

import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.jooq.SelectJoinStep;

import static org.jooq.generated.Tables.BOOKMARKS;
import static org.jooq.generated.Tables.POST_LIKE;
import static org.jooq.generated.tables.Posts.POSTS;
import static org.jooq.impl.DSL.*;

@RequiredArgsConstructor
public class LoginUserQueryStrategy implements ProjectQueryStrategy {
    private final Long userId;

    @Override
    public SelectJoinStep<?> buildJoinQuery(SelectJoinStep<?> query) {
        return query
                .leftJoin(BOOKMARKS.as("user_bookmarks"))
                .on(BOOKMARKS.as("user_bookmarks").POST_ID.eq(POSTS.ID)
                        .and(BOOKMARKS.as("user_bookmarks").USER_ID.eq(userId)))
                .leftJoin(POST_LIKE.as("user_likes"))
                .on(POST_LIKE.as("user_likes").POST_ID.eq(POSTS.ID)
                        .and(POST_LIKE.as("user_likes").USER_ID.eq(userId)));
    }

    @Override
    public Field<Boolean> buildHasBookmarkedField() {
        return case_()
                .when(max(case_()
                        .when(BOOKMARKS.as("user_bookmarks").USER_ID.eq(userId), 1)
                        .else_(0)).gt(0), val(true))
                .else_(val(false))
                .as("hasBookmarked");
    }

    @Override
    public Field<Boolean> buildHasLikedField() {
        return case_()
                .when(max(case_()
                        .when(POST_LIKE.as("user_likes").USER_ID.eq(userId), 1)
                        .else_(0)).gt(0), val(true))
                .else_(val(false))
                .as("hasLiked");
    }
}
