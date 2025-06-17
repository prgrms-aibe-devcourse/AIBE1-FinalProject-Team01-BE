package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService implements BasePostService {
    private final PostRepository postRepository;

    @Override
    public void createPost() {
        System.out.println("this is post service!!");
    }

    @Override
    public void reatPost() {

    }

    @Override
    public void readPosts() {

    }

    @Override
    public void updatePost() {

    }

    @Override
    public void deletePost() {

    }
}
