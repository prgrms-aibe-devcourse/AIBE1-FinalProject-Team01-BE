package kr.co.amateurs.server.repository.file;

import kr.co.amateurs.server.domain.entity.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
