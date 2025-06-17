package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
