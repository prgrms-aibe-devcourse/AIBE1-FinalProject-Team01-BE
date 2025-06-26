package kr.co.amateurs.server.repository.like;

import kr.co.amateurs.server.domain.entity.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
}
