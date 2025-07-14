package kr.co.amateurs.server.repository.comment;

import kr.co.amateurs.server.domain.dto.comment.CommentJooqDTO;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.repository.comment.strategy.CommentQueryStrategy;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.generated.Tables.*;

@Repository
@RequiredArgsConstructor
public class CommentJooqRepository {

    private final DSLContext dsl;

    public List<CommentJooqDTO> findRootCommentsWithLikes(Long postId, Long userId, PageRequest pageRequest) {
        return findRootComments(postId, userId, null, pageRequest);
    }

    public List<CommentJooqDTO> findRootCommentsAfterCursorWithLikes(Long postId, Long cursor, Long userId, PageRequest pageRequest) {
        return findRootComments(postId, userId, cursor, pageRequest);
    }

    public List<CommentJooqDTO> findRootCommentsForGuest(Long postId, PageRequest pageRequest) {
        return findRootComments(postId, null, null, pageRequest);
    }

    public List<CommentJooqDTO> findRootCommentsAfterCursorForGuest(Long postId, Long cursor, PageRequest pageRequest) {
        return findRootComments(postId, null, cursor, pageRequest);
    }

    public List<CommentJooqDTO> findRepliesWithLikes(Long parentCommentId, Long userId, PageRequest pageRequest) {
        return findReplies(parentCommentId, userId, null, pageRequest);
    }

    public List<CommentJooqDTO> findRepliesAfterCursorWithLikes(Long parentCommentId, Long cursor, Long userId, PageRequest pageRequest) {
        return findReplies(parentCommentId, userId, cursor, pageRequest);
    }

    public List<CommentJooqDTO> findRepliesForGuest(Long parentCommentId, PageRequest pageRequest) {
        return findReplies(parentCommentId, null, null, pageRequest);
    }

    public List<CommentJooqDTO> findRepliesAfterCursorForGuest(Long parentCommentId, Long cursor, PageRequest pageRequest) {
        return findReplies(parentCommentId, null, cursor, pageRequest);
    }

    private List<CommentJooqDTO> findRootComments(Long postId, Long userId, Long cursor, PageRequest pageRequest) {
        CommentQueryStrategy strategy = (userId != null)
                ? CommentQueryStrategy.forLoginUser(userId)
                : CommentQueryStrategy.forGuest();

        var query = buildCommentQuery(strategy)
                .where(COMMENTS.POST_ID.eq(postId))
                .and(COMMENTS.PARENT_COMMENT_ID.isNull())
                .and(COMMENTS.IS_DELETED.eq(false));

        if (cursor != null) {
            query = query.and(COMMENTS.ID.gt(cursor));
        }

        return query
                .orderBy(COMMENTS.CREATED_AT.asc())
                .limit(pageRequest.getPageSize())
                .fetch(record -> mapToCommentJooqDTO(record, userId != null));
    }

    private List<CommentJooqDTO> findReplies(Long parentCommentId, Long userId, Long cursor, PageRequest pageRequest) {
        CommentQueryStrategy strategy = (userId != null)
                ? CommentQueryStrategy.forLoginUser(userId)
                : CommentQueryStrategy.forGuest();

        var query = buildCommentQuery(strategy)
                .where(COMMENTS.PARENT_COMMENT_ID.eq(parentCommentId))
                .and(COMMENTS.IS_DELETED.eq(false));

        if (cursor != null) {
            query = query.and(COMMENTS.ID.gt(cursor));
        }

        return query
                .orderBy(COMMENTS.CREATED_AT.asc())
                .limit(pageRequest.getPageSize())
                .fetch(record -> mapToCommentJooqDTO(record, userId != null));
    }

    private SelectJoinStep<?> buildCommentQuery(CommentQueryStrategy strategy) {
        var selectQuery = strategy.buildSelectQuery(dsl);
        var fromQuery = selectQuery.from(COMMENTS);
        return strategy.buildJoinQuery(fromQuery);
    }

    private CommentJooqDTO mapToCommentJooqDTO(Record record, boolean includeHasLiked) {
        DevCourseTrack devCourseTrack = parseDevCourseTrack(record.get(USERS.DEVCOURSE_NAME));
        Boolean hasLiked = includeHasLiked ? record.get("has_liked", Boolean.class) : false;

        return CommentJooqDTO.builder()
                .id(record.get(COMMENTS.ID))
                .postId(record.get(COMMENTS.POST_ID))
                .isBlinded(record.get(COMMENTS.IS_BLINDED))
                .nickname(record.get(USERS.NICKNAME))
                .profileImageUrl(record.get(USERS.IMAGE_URL))
                .devCourseTrack(devCourseTrack)
                .parentCommentId(record.get(COMMENTS.PARENT_COMMENT_ID))
                .content(record.get(COMMENTS.CONTENT))
                .replyCount(record.get(COMMENTS.REPLY_COUNT))
                .likeCount(record.get(COMMENTS.LIKE_COUNT))
                .hasLiked(hasLiked != null ? hasLiked : false)
                .createdAt(record.get(COMMENTS.CREATED_AT))
                .updatedAt(record.get(COMMENTS.UPDATED_AT))
                .build();
    }

    private DevCourseTrack parseDevCourseTrack(String devCourseNameStr) {
        if (devCourseNameStr == null) {
            return null;
        }
        try {
            return DevCourseTrack.valueOf(devCourseNameStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}