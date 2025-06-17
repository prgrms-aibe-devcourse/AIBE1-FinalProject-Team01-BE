package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService implements BasePostService {
    private final PostRepository postRepository;

    @Override
    public String createPost() {
        return "this is post service create method!!";
    }

    @Override
    public String readPost() {
        return "this is post service read method!!";
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
