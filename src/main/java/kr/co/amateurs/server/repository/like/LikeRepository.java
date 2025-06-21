package kr.co.amateurs.server.repository.like;

import kr.co.amateurs.server.domain.entity.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
}
