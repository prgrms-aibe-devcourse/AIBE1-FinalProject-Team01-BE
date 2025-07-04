package kr.co.amateurs.server.repository.file;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    boolean existsByUrl(String fileUrl);

    List<PostImage> findByPost(Post post);
}
