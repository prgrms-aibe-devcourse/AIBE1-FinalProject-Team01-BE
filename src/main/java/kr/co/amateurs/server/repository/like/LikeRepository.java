package kr.co.amateurs.server.repository.like;

import kr.co.amateurs.server.domain.entity.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByPostIdAndUserId(Long postId, Long id);

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
    Optional<Like> findByPost_IdAndUser_Id(Long postId, Long id);

    boolean existsByPost_IdAndUser_Id(Long postId, Long id);
}
