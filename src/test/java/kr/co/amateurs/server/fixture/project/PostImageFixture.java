package kr.co.amateurs.server.fixture.project;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostImage;

public class PostImageFixture {
    public static PostImage createPostImage(Post post, String imageUrl) {
        return PostImage.builder()
                .post(post)
                .imageUrl(imageUrl)
                .build();
    }
}
