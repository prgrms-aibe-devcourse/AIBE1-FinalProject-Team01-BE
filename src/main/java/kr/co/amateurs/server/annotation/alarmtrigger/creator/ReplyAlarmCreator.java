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

/**
 * 대댓글 관련 알람을 생성하는 구현체입니다.
 * 
 * 대댓글이 작성되었을 때 원댓글 작성자에게 답글 알람을 전송합니다.
 * CommentAlarmCreator에서 대댓글 감지 시 호출되어 사용됩니다.
 * 
 * 처리 대상:
 * - CommentResponseDTO 타입의 결과 객체 (parentCommentId가 null이 아닌 경우)
 * - AlarmType.REPLY 타입의 알람
 * 
 * 알람 발송 로직:
 * - 원댓글 작성자에게 답글 알람 전송
 * - parentCommentId가 null인 경우 예외 발생
 */
@Component
@RequiredArgsConstructor
public class ReplyAlarmCreator implements AlarmCreator {

    private final AlarmService alarmService;
    private final CommentService commentService;

    /**
     * 대댓글 생성 결과를 기반으로 알람을 생성합니다.
     * 
     * parentCommentId를 통해 원댓글을 조회하고,
     * 원댓글 작성자에게 답글 알람을 전송합니다.
     * 
     * @param result CommentResponseDTO 타입의 대댓글 생성 결과
     * @throws CustomException 지원하지 않는 결과 타입이거나 parentCommentId가 null인 경우
     */
    @Override
    public void createAlarm(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        if (response.parentCommentId() == null) {
            throw new CustomException(ErrorCode.NOT_FOUND);
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

    /**
     * 답글 알람의 내용 메시지를 생성합니다.
     * 
     * @param nickname 원댓글 작성자의 닉네임
     * @param comment 답글이 달린 원댓글
     * @return 알람 내용 메시지
     */
    public String getContent(String nickname, Comment comment) {
        String title = comment.getContent().length() > 20
                ? comment.getContent().substring(0, 17) + "..."
                : comment.getContent();
        return nickname + "님이 작성하신 \"" + title + "\"에 답글을 달았습니다.";
    }

    /**
     * 답글 알람의 메타데이터를 생성합니다.
     * 
     * @param response 댓글 응답 DTO
     * @return 게시글 ID와 댓글 ID를 포함한 메타데이터
     */
    public AlarmMetaData getMetaData(CommentResponseDTO response) {
        return new CommentMetaData(response.postId(), response.id());
    }
}
