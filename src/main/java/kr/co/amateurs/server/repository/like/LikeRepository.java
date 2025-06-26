package kr.co.amateurs.server.repository.like;

import kr.co.amateurs.server.domain.entity.like.Like;
import kr.co.amateurs.server.domain.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM Like l WHERE l.comment.id = :commentId AND l.user.id = :userId")
    int deleteByCommentAndUser(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Like l WHERE l.post.id = :postId AND l.user.id = :userId")
    int deleteByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    Optional<Like> findByPost_IdAndUser_Id(Long postId, Long id);

    @Query("SELECT l.comment.id FROM Like l WHERE l.comment.id IN :commentIds AND l.user.id = :userId")
    Set<Long> findByCommentIdsAndUserId(@Param("commentIds") List<Long> commentIds, @Param("userId") Long userId);
}
