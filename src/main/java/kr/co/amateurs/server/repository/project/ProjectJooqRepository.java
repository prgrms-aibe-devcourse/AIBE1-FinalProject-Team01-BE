package kr.co.amateurs.server.repository.project;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.project.ProjectResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectSearchParam;
import kr.co.amateurs.server.repository.project.strategy.ProjectQueryStrategy;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.generated.Tables.BOOKMARKS;
import static org.jooq.generated.Tables.POST_STATISTICS;
import static org.jooq.generated.Tables.POST_IMAGES;
import static org.jooq.generated.tables.Posts.POSTS;
import static org.jooq.generated.tables.Projects.PROJECTS;
import static org.jooq.generated.tables.Users.USERS;
import static org.jooq.impl.DSL.noCondition;

@Repository
@RequiredArgsConstructor
public class ProjectJooqRepository {
    private final DSLContext dslContext;

    public Page<ProjectResponseDTO> findAllByUserId(ProjectSearchParam params, Long userId) {
        ProjectQueryStrategy strategy = ProjectQueryStrategy.forLoginUser(userId);
        return executeProjectListQuery(strategy, params);
    }

    public Page<ProjectResponseDTO> findAll(ProjectSearchParam params) {
        ProjectQueryStrategy strategy = ProjectQueryStrategy.forAnonymousUser();
        return executeProjectListQuery(strategy, params);
    }

    public ProjectResponseDTO findByIdAndUserId(Long projectId, Long userId) {
        ProjectQueryStrategy strategy = ProjectQueryStrategy.forLoginUser(userId);
        return executeProjectDetailsQuery(strategy, projectId);
    }

    public ProjectResponseDTO findById(Long projectId) {
        ProjectQueryStrategy strategy = ProjectQueryStrategy.forAnonymousUser();
        return executeProjectDetailsQuery(strategy, projectId);
    }

    private Page<ProjectResponseDTO> executeProjectListQuery(ProjectQueryStrategy strategy, ProjectSearchParam params) {
        var selectQuery = strategy.buildSelectQuery(dslContext);
        var joinQuery = buildJoinQuery(selectQuery);
        var finalQuery = strategy.buildJoinQuery(joinQuery);
        var results = fetchProjectList(finalQuery, params);
        int total = getTotalCount(params);

        return new PageImpl<>(results, params.toPageable(), total);
    }

    private ProjectResponseDTO executeProjectDetailsQuery(ProjectQueryStrategy strategy, Long projectId) {
        var selectQuery = strategy.buildSelectQuery(dslContext);
        var joinQuery = buildJoinQuery(selectQuery);
        var finalQuery = strategy.buildJoinQuery(joinQuery);

        return finalQuery
                .where(PROJECTS.ID.eq(projectId))
                .fetchOptionalInto(ProjectResponseDTO.class)
                .orElseThrow(ErrorCode.POST_NOT_FOUND);
    }

    private SelectJoinStep<?> buildJoinQuery(SelectSelectStep<?> selectQuery) {
        return selectQuery.from(PROJECTS)
                .join(POSTS).on(PROJECTS.POST_ID.eq(POSTS.ID))
                .join(USERS).on(POSTS.USER_ID.eq(USERS.ID))
                .join(POST_STATISTICS).on(POSTS.ID.eq(POST_STATISTICS.POST_ID));
    }

    private List<ProjectResponseDTO> fetchProjectList(SelectJoinStep<?> query, ProjectSearchParam params) {
        Condition condition = buildConditions(params);

        return query
                .where(condition)
                .orderBy(buildOrderBy(params))
                .limit(params.getSize())
                .offset(params.getPage() * params.getSize())
                .fetchInto(ProjectResponseDTO.class);
    }

    private int getTotalCount(ProjectSearchParam params) {
        Condition condition = buildConditions(params);

        return dslContext.selectCount()
                .from(PROJECTS)
                .join(POSTS).on(PROJECTS.POST_ID.eq(POSTS.ID))
                .join(USERS).on(POSTS.USER_ID.eq(USERS.ID))
                .where(condition)
                .fetchOptional(0, int.class)
                .orElse(0);
    }

    private Condition buildConditions(ProjectSearchParam params) {
        Condition condition = noCondition();

        if (params.getCourse() != null) {
            condition = condition.and(USERS.DEVCOURSE_NAME.eq(params.getCourse().name()));
        }

        if (params.getBatch() != null) {
            condition = condition.and(USERS.DEVCOURSE_BATCH.eq(params.getBatch()));
        }

        if (params.getKeyword() != null) {
            String keyword = "%" + params.getKeyword() + "%";
            condition = condition.and(POSTS.TITLE.like(keyword).or(POSTS.CONTENT.like(keyword)));
        }

        return condition;
    }

    private OrderField<?> buildOrderBy(ProjectSearchParam params) {
        return switch (params.getField()) {
            case ID -> params.getSortDirection() == Sort.Direction.ASC ?
                    PROJECTS.ID.asc() : PROJECTS.ID.desc();
            case POST_LATEST -> params.getSortDirection() == Sort.Direction.ASC ?
                    POSTS.CREATED_AT.asc() : POSTS.CREATED_AT.desc();
            case POST_POPULAR -> params.getSortDirection() == Sort.Direction.ASC ?
                    POSTS.LIKE_COUNT.asc() : POSTS.LIKE_COUNT.desc();
            case POST_MOST_VIEW -> params.getSortDirection() == Sort.Direction.ASC ?
                    POST_STATISTICS.VIEW_COUNT.asc() : POST_STATISTICS.VIEW_COUNT.desc();
            default -> PROJECTS.ID.desc();
        };
    }
}
