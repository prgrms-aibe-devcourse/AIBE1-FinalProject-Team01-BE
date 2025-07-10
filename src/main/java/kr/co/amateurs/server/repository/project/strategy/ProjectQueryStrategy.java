package kr.co.amateurs.server.repository.project.strategy;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;

import static org.jooq.generated.Tables.BOOKMARKS;
import static org.jooq.generated.Tables.POST_IMAGES;
import static org.jooq.generated.Tables.POST_STATISTICS;
import static org.jooq.generated.tables.Posts.POSTS;
import static org.jooq.generated.tables.Projects.PROJECTS;
import static org.jooq.generated.tables.Users.USERS;

public interface ProjectQueryStrategy {
    default SelectSelectStep<?> buildSelectQuery(DSLContext dslContext) {
        return dslContext.select(
                PROJECTS.ID,
                PROJECTS.POST_ID,
                PROJECTS.STARTED_AT,
                PROJECTS.ENDED_AT,
                PROJECTS.GITHUB_URL,
                PROJECTS.SIMPLE_CONTENT,
                PROJECTS.DEMO_URL,
                PROJECTS.PROJECT_MEMBERS,

                POSTS.USER_ID.as("authorId"),
                POSTS.TITLE,
                POSTS.CONTENT,
                POSTS.TAG,
                POST_STATISTICS.VIEW_COUNT,
                POSTS.LIKE_COUNT,
                dslContext.selectCount()
                        .from(BOOKMARKS)
                        .where(BOOKMARKS.POST_ID.eq(POSTS.ID))
                        .asField("bookmarkCount"),

                POSTS.CREATED_AT,
                POSTS.UPDATED_AT,

                USERS.NICKNAME,
                USERS.DEVCOURSE_NAME,
                USERS.DEVCOURSE_BATCH,
                dslContext.select(POST_IMAGES.IMAGE_URL)
                        .from(POST_IMAGES)
                        .where(POST_IMAGES.POST_ID.eq(POSTS.ID))
                        .orderBy(POST_IMAGES.CREATED_AT.asc())
                        .limit(1)
                        .asField("thumbnailImageUrl"),

                buildHasBookmarkedField(),
                buildHasLikedField()
        );
    }

    SelectJoinStep<?> buildJoinQuery(SelectJoinStep<?> query);

    Field<Boolean> buildHasBookmarkedField();

    Field<Boolean> buildHasLikedField();

    static ProjectQueryStrategy forAnonymousUser() {
        return new AnonymousUserQueryStrategy();
    }

    static ProjectQueryStrategy forLoginUser(Long userId) {
        return new LoginUserQueryStrategy(userId);
    }
}
