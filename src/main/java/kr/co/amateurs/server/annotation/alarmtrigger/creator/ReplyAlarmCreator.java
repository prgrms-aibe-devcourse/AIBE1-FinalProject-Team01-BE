package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import kr.co.amateurs.server.domain.entity.alarm.metadata.CommentMetaData;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.alarm.AlarmService;
import kr.co.amateurs.server.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplyAlarmCreator implements AlarmCreator {

    private final AlarmService alarmService;
    private final CommentService commentService;

    @Override
    public void createAlarm(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        if (response.parentCommentId() == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_ROOM);
        }

        Comment comment = commentService.findCommentById(response.parentCommentId());
        User commentAuthor = comment.getUser();
        Alarm alarm = Alarm.builder()
                .userId(commentAuthor.getId())
                .type(AlarmType.REPLY)
                .title(AlarmType.REPLY.getTitle())
                .content(getContent(commentAuthor.getNickname(), comment))
                .metaData(getMetaData(response))
                .build();

        alarmService.saveAlarm(alarm);
    }

    @Override
    public AlarmType getType() {
        return AlarmType.REPLY;
    }

    public String getContent(String nickname, Comment comment) {
        String title = comment.getContent().length() > 20
                ? comment.getContent().substring(0, 17) + "..."
                : comment.getContent();
        return nickname + "님이 작성하신 \"" + title + "\"에 답글을 달았습니다.";
    }

    public AlarmMetaData getMetaData(CommentResponseDTO response) {
        return new CommentMetaData(response.postId(), response.id());
    }
}
