package kr.co.amateurs.server.repository.together;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import static org.jooq.generated.tables.MarketItems.MARKET_ITEMS;
import static org.jooq.generated.tables.Posts.POSTS;

@Repository
public class MarketJooqRepository {

    private final DSLContext dsl;

    public MarketJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public int countByKeyword(String keyword) {
        String pattern = "%" + keyword.toLowerCase() + "%";

        return dsl.select(DSL.count())
                .from(MARKET_ITEMS)
                .join(POSTS)
                .on(MARKET_ITEMS.POST_ID.eq(POSTS.ID))
                .where(
                        DSL.lower(POSTS.TITLE).like(pattern)
                                .or(DSL.lower(POSTS.CONTENT).like(pattern))
                )
                .fetchOne(0, int.class);
    }
}