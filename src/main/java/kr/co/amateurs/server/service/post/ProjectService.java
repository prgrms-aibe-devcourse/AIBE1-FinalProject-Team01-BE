package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.repository.post.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService implements BasePostService {
    private final ProjectRepository projectRepository;

    @Override
    public String createPost() {
        return "this is project service create method!!!!";
    }

    @Override
    public String readPost() {
        return "this is project service read method!!!!";
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
