package kr.co.amateurs.server.controller.post;

import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.service.post.BasePostService;
import kr.co.amateurs.server.service.post.MarketService;
import kr.co.amateurs.server.service.post.PostService;
import kr.co.amateurs.server.service.post.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostServiceFactory {
    private final PostService postService;
    private final MarketService marketService;
    private final ProjectService projectService;

    public BasePostService getService(BoardType boardType) {
        return switch (boardType) {
            case FREE, STUDENT, GATHER, RETROSPECT, MATCH, INFO -> postService;
            case MARKET -> marketService;
            case PROJECT_HUB -> projectService;
        };
    }
}
