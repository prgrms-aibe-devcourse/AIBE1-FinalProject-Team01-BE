package kr.co.amateurs.server.repository.comment.strategy;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;

import static org.jooq.generated.Tables.COMMENTS;
import static org.jooq.generated.Tables.USERS;

public interface CommentQueryStrategy {
    default SelectSelectStep<?> buildSelectQuery(DSLContext dslContext) {
        return dslContext.select(
                COMMENTS.ID,
                COMMENTS.POST_ID,
                COMMENTS.IS_BLINDED,
                USERS.NICKNAME,
                USERS.IMAGE_URL,
                USERS.DEVCOURSE_NAME,
                COMMENTS.PARENT_COMMENT_ID,
                COMMENTS.CONTENT,
                COMMENTS.REPLY_COUNT,
                COMMENTS.LIKE_COUNT,
                COMMENTS.CREATED_AT,
                COMMENTS.UPDATED_AT,
                buildHasLikedField()
        );
    }

    SelectJoinStep<?> buildJoinQuery(SelectJoinStep<?> query);
    Field<Boolean> buildHasLikedField();

    static CommentQueryStrategy forGuest() {
        return new GuestCommentQueryStrategy();
    }

    static CommentQueryStrategy forLoginUser(Long userId) {
        return new LoginUserCommentQueryStrategy(userId);
    }
}
