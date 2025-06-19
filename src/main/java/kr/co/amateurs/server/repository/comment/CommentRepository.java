package kr.co.amateurs.server.repository.comment;

import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtAsc(Post post, PageRequest pageRequest);

    List<Comment> findByPostAndParentCommentIsNullAndIdGreaterThanOrderByCreatedAtAsc(Post post, Long cursor, PageRequest pageRequest);

    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment, PageRequest pageRequest);

    List<Comment> findByParentCommentAndIdGreaterThanOrderByCreatedAtAsc(Comment parentComment, Long cursor, PageRequest pageRequest);

    // TODO QueryDSL or JOOQ 사용 시 변경
    @Query("SELECT c.parentComment.id, COUNT(c) FROM Comment c " +
            "WHERE c.parentComment.id IN :parentIds " +
            "GROUP BY c.parentComment.id")
    List<Object[]> countRepliesByParentIds(@Param("parentIds") List<Long> parentIds);
}
