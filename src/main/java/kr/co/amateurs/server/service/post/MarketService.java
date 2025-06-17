package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.repository.post.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketService implements BasePostService {
    private final MarketRepository marketRepository;

    @Override
    public void createPost() {
        System.out.println("this is market service!!!!!!!");
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
