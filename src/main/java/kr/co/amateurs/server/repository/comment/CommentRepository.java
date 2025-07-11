package kr.co.amateurs.server.repository.comment;

import kr.co.amateurs.server.domain.entity.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :commentId")
    void increaseReplyCount(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = GREATEST(c.replyCount - 1, 0) WHERE c.id = :commentId")
    void decreaseReplyCount(@Param("commentId") Long commentId);

    Optional<Comment> findByIdAndPostId(Long parentCommentId, Long postId);

    void deleteByPostId(Long id);
}
