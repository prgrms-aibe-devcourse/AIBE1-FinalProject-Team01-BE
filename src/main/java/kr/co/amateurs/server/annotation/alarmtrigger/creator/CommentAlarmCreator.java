package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import kr.co.amateurs.server.domain.entity.alarm.metadata.CommentMetaData;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.alarm.AlarmService;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 댓글 관련 알람을 생성하는 구현체입니다.
 * 
 * 댓글이 작성되었을 때 게시글 작성자에게 알람을 전송하며,
 * 대댓글인 경우 추가로 원댓글 작성자에게도 답글 알람을 전송합니다.
 * 
 * 처리 대상:
 * - CommentResponseDTO 타입의 결과 객체
 * - AlarmType.COMMENT 타입의 알람
 * 
 * 알람 발송 로직:
 * 1. 일반 댓글: 게시글 작성자에게 댓글 알람
 * 2. 대댓글: 게시글 작성자에게 댓글 알람 + 원댓글 작성자에게 답글 알람
 */
@Component
@RequiredArgsConstructor
public class CommentAlarmCreator implements AlarmCreator {

    private final AlarmService alarmService;
    private final CommunityPostService postService;
    private final ReplyAlarmCreator replyAlarmCreator;

    /**
     * 댓글 생성 결과를 기반으로 알람을 생성합니다.
     * 
     * 게시글 작성자에게 댓글 알람을 전송하며,
     * 대댓글인 경우 ReplyAlarmCreator를 통해 추가 알람을 생성합니다.
     * 
     * @param result CommentResponseDTO 타입의 댓글 생성 결과
     * @throws CustomException 지원하지 않는 결과 타입인 경우
     */
    @Override
    public void createAlarm(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        Post post = postService.findById(response.postId());
        User postAuthor = post.getUser();
        Alarm alarm = Alarm.builder()
                .userId(postAuthor.getId())
                .type(AlarmType.COMMENT)
                .title(AlarmType.COMMENT.getTitle())
                .content(getContent(postAuthor.getNickname(), post))
                .metaData(getMetaData(response))
                .build();

        alarmService.saveAlarm(alarm);

        if (response.parentCommentId() != null) {
            replyAlarmCreator.createAlarm(result);
        }
    }

    @Override
    public AlarmType getType() {
        return AlarmType.COMMENT;
    }

    /**
     * 댓글 알람의 내용 메시지를 생성합니다.
     * 
     * @param nickname 게시글 작성자의 닉네임
     * @param post 댓글이 달린 게시글
     * @return 알람 내용 메시지
     */
    public String getContent(String nickname, Post post) {
        String title = post.getTitle().length() > 20
                ? post.getTitle().substring(0, 17) + "..."
                : post.getTitle();
        return nickname + "님이 작성하신 \"" + title + "\"에 댓글을 달았습니다.";
    }

    /**
     * 댓글 알람의 메타데이터를 생성합니다.
     * 
     * @param response 댓글 응답 DTO
     * @return 게시글 ID와 댓글 ID를 포함한 메타데이터
     */
    public AlarmMetaData getMetaData(CommentResponseDTO response) {
        return new CommentMetaData(response.postId(), response.id());
    }
}
