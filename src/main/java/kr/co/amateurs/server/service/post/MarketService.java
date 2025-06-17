package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.repository.post.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketService implements BasePostService {
    private final MarketRepository marketRepository;

    @Override
    public String createPost() {
        return "this is market service create method!!!!!!!";
    }

    @Override
    public String readPost() {
        return "this is market service read method!!!!!!!";
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
