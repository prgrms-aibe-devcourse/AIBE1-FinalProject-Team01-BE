package kr.co.amateurs.server.controller.post;

import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostServiceFactory postServiceFactory;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createPost(@RequestParam("type") BoardType boardType) {
        return postServiceFactory.getService(boardType).createPost();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String readPost(@RequestParam("type") BoardType boardType) {
        return postServiceFactory.getService(boardType).readPost();
    }
}
