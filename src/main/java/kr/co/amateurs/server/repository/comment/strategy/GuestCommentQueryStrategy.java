package kr.co.amateurs.server.repository.comment.strategy;

import org.jooq.Field;
import org.jooq.SelectJoinStep;

import static org.jooq.generated.Tables.*;
import static org.jooq.impl.DSL.val;

public class GuestCommentQueryStrategy implements CommentQueryStrategy {

    @Override
    public SelectJoinStep<?> buildJoinQuery(SelectJoinStep<?> query) {
        return query.join(USERS).on(COMMENTS.USER_ID.eq(USERS.ID));
    }

    @Override
    public Field<Boolean> buildHasLikedField() {
        return val(false).as("has_liked");
    }
}
