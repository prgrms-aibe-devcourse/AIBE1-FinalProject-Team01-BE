package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.comment.CommentService;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlarmFacade {
    private final AlarmService alarmService;
    private final UserService userService;
    private final CommunityPostService postService;
    private final CommentService commentService;

    public void readAll() {
        User user = userService.getCurrentUser()
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        alarmService.readAll(user.getId());
    }
}
