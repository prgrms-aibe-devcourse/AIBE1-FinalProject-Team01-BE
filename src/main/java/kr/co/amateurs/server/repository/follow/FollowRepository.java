package kr.co.amateurs.server.repository.follow;

import kr.co.amateurs.server.domain.entity.follow.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
}
