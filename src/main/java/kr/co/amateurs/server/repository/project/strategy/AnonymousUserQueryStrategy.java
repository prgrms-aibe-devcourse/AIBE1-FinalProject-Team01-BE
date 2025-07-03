package kr.co.amateurs.server.repository.project.strategy;

import org.jooq.Field;
import org.jooq.SelectJoinStep;

import static org.jooq.impl.DSL.val;

public class AnonymousUserQueryStrategy implements ProjectQueryStrategy {

    @Override
    public SelectJoinStep<?> buildJoinQuery(SelectJoinStep<?> query) {
        return query;
    }

    @Override
    public Field<Boolean> buildHasBookmarkedField() {
        return val(false).as("hasBookmarked");
    }

    @Override
    public Field<Boolean> buildHasLikedField() {
        return val(false).as("hasLiked");
    }
}
