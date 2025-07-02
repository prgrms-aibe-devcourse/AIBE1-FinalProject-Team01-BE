package kr.co.amateurs.server.controller.follow;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.repository.follow.FollowRepository;
import kr.co.amateurs.server.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "유저 간 팔로우 API")
public class FollowController {
    private final FollowService followService;

}
