package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<GatheringPost, Long> {
}
