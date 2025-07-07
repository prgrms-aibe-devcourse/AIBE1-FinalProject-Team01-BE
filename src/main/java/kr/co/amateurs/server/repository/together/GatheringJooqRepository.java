package kr.co.amateurs.server.repository.together;

import org.jooq.DSLContext;
import org.jooq.generated.tables.Posts;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import static org.jooq.generated.tables.GatheringPosts.GATHERING_POSTS;
import static org.jooq.generated.tables.Posts.POSTS;

@Repository
public class GatheringJooqRepository {

    private final DSLContext dsl;

    public GatheringJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public int countByKeyword(String keyword) {
        String pattern = "%" + keyword.toLowerCase() + "%";

        return dsl.select(DSL.count())
                .from(GATHERING_POSTS)
                .join(POSTS)
                .on(GATHERING_POSTS.POST_ID.eq(POSTS.ID))
                .where(
                        DSL.lower(POSTS.TITLE).like(pattern)
                                .or(DSL.lower(POSTS.CONTENT).like(pattern))
                )
                .fetchOne(0, int.class);
    }
}