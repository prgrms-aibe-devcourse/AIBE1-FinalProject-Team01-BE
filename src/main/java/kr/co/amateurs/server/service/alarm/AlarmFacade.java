package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.domain.dto.alarm.AlarmPageResponse;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
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

    public AlarmPageResponse readAlarms(PaginationParam param) {
        User user = userService.getCurrentLoginUser();
        return alarmService.readAlarms(user.getId(), param);
    }

    public void markAllAsRead() {
        User user = userService.getCurrentLoginUser();
        alarmService.markAllAsRead(user.getId());
    }

    public void markAsRead(String alarmId) {
        User user = userService.getCurrentLoginUser();
        alarmService.markAsRead(user.getId(), alarmId);
    }
}
