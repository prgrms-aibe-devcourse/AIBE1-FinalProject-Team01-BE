package kr.co.amateurs.server.repository.like;

import kr.co.amateurs.server.domain.dto.like.CommentLikeStatusDTO;
import kr.co.amateurs.server.domain.entity.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.time.LocalDateTime;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Transactional
    @Modifying
    void deleteByPostIdAndUserId(Long postId, Long userId);

    @Transactional
    @Modifying
    void deleteByCommentIdAndUserId(Long commentId, Long userId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void increasePostLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void increaseCommentLikeCount(@Param("commentId") Long commentId);
    @Query("SELECT new kr.co.amateurs.server.domain.dto.like.CommentLikeStatusDTO(c.id, " +
            "CASE WHEN l.id IS NOT NULL THEN true ELSE false END) " +
            "FROM Comment c LEFT JOIN Like l ON c.id = l.comment.id AND l.user.id = :userId " +
            "WHERE c.id IN :commentIds")
    List<CommentLikeStatusDTO> findCommentLikeStatusByCommentIdsAndUserId(
            @Param("commentIds") List<Long> commentIds,
            @Param("userId") Long userId
    );

    boolean existsByPost_IdAndUser_Id(Long postId, Long id);

    List<Like> findTop3ByUserIdAndPostIsNotNullOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndPostIsNotNullAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    boolean existsByComment_IdAndUser_Id(Long commentId, Long userId);
}
