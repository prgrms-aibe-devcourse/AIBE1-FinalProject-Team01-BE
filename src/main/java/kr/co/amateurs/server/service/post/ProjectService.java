package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.repository.post.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService implements BasePostService{
    private final ProjectRepository projectRepository;

    @Override
    public void createPost() {
        System.out.println("this is project service!!!");
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
