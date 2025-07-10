package kr.co.amateurs.server.repository.project.strategy;

import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import static org.jooq.generated.Tables.BOOKMARKS;
import static org.jooq.generated.Tables.POST_LIKE;
import static org.jooq.generated.tables.Posts.POSTS;

@RequiredArgsConstructor
public class LoginUserQueryStrategy implements ProjectQueryStrategy {
    private final Long userId;

    @Override
    public SelectJoinStep<?> buildJoinQuery(SelectJoinStep<?> query) {
        return query;
    }

    @Override
    public Field<Boolean> buildHasBookmarkedField() {
        return DSL.exists(
                DSL.selectOne()
                        .from(BOOKMARKS)
                        .where(BOOKMARKS.POST_ID.eq(POSTS.ID))
                        .and(BOOKMARKS.USER_ID.eq(userId))
        ).as("hasBookmarked");
    }

    @Override
    public Field<Boolean> buildHasLikedField() {
        return DSL.exists(
                DSL.selectOne()
                        .from(POST_LIKE)
                        .where(POST_LIKE.POST_ID.eq(POSTS.ID))
                        .and(POST_LIKE.USER_ID.eq(userId))
        ).as("hasLiked");
    }
}
