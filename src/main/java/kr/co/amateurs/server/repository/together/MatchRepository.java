package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<MatchingPost, Long> {

}
