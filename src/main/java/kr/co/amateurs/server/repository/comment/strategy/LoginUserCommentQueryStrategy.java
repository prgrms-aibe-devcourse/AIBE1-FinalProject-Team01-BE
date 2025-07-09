package kr.co.amateurs.server.repository.comment.strategy;

import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.jooq.SelectJoinStep;

import static org.jooq.generated.Tables.*;
import static org.jooq.impl.DSL.case_;

@RequiredArgsConstructor
public class LoginUserCommentQueryStrategy implements CommentQueryStrategy {
    private final Long userId;

    @Override
    public SelectJoinStep<?> buildJoinQuery(SelectJoinStep<?> query) {
        return query
                .join(USERS).on(COMMENTS.USER_ID.eq(USERS.ID))
                .leftJoin(POST_LIKE).on(
                        POST_LIKE.COMMENT_ID.eq(COMMENTS.ID)
                                .and(POST_LIKE.USER_ID.eq(userId))
                );
    }

    @Override
    public Field<Boolean> buildHasLikedField() {
        return case_()
                .when(POST_LIKE.ID.isNotNull(), true)
                .otherwise(false).as("has_liked");
    }
}
