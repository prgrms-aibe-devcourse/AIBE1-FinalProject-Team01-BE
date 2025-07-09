package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import kr.co.amateurs.server.domain.entity.alarm.metadata.CommentMetaData;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.alarm.AlarmService;
import kr.co.amateurs.server.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import kr.co.amateurs.server.service.post.PostService;
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
    private final UserService userService;
    private final CommentService commentService;
    private final PostService postService;

    /**
     * 댓글 생성 결과를 기반으로 알람을 생성합니다.
     *
     * 게시글 작성자에게 댓글 알람을 전송하며,
     * 대댓글인 경우 원댓글 작성자에게도 답글 알람을 전송합니다.
     * 자신이 작성한 게시글/댓글에 자신이 댓글을 단 경우 알람을 전송하지 않습니다.
     *
     * @param result CommentResponseDTO 타입의 댓글 생성 결과
     * @throws CustomException 지원하지 않는 결과 타입인 경우
     */
    @Override
    public void createAlarm(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        User currentUser = userService.getCurrentLoginUser();

        Post post = postService.findById(response.postId());

        createCommentAlarm(currentUser, post, response);

        if (response.parentCommentId() != null) {
            createReplyAlarm(currentUser, response);
        }
    }

    /**
     * 게시글 작성자에게 댓글 알람을 전송합니다.
     * 자신의 게시글에 자신이 댓글을 단 경우 알람을 전송하지 않습니다.
     */
    private void createCommentAlarm(User commentAuthor, Post post, CommentResponseDTO response) {
        User postAuthor = post.getUser();

        if (isSameUser(commentAuthor, postAuthor)) {
            return;
        }

        Alarm alarm = Alarm.builder()
                .userId(postAuthor.getId())
                .type(AlarmType.COMMENT)
                .title(AlarmType.COMMENT.getTitle())
                .content(getCommentAlarmContent(commentAuthor.getNickname(), post))
                .metaData(getMetaData(response))
                .build();

        alarmService.saveAlarm(alarm);
    }

    /**
     * 원댓글 작성자에게 답글 알람을 전송합니다.
     * 자신의 댓글에 자신이 답글을 단 경우 알람을 전송하지 않습니다.
     */
    private void createReplyAlarm(User replyAuthor, CommentResponseDTO response) {
        Comment parentComment = commentService.findCommentById(response.parentCommentId());
        User commentAuthor = parentComment.getUser();

        if (isSameUser(replyAuthor, commentAuthor)) {
            return;
        }

        Alarm alarm = Alarm.builder()
                .userId(commentAuthor.getId())
                .type(AlarmType.REPLY)
                .title(AlarmType.REPLY.getTitle())
                .content(getReplyAlarmContent(replyAuthor.getNickname(), parentComment))
                .metaData(getMetaData(response))
                .build();

        alarmService.saveAlarm(alarm);
    }

    /**
     * 두 사용자가 같은 사용자인지 확인합니다.
     */
    private boolean isSameUser(User user1, User user2) {
        return user1.getId().equals(user2.getId());
    }

    @Override
    public AlarmType getType() {
        return AlarmType.COMMENT;
    }

    /**
     * 댓글 알람의 내용 메시지를 생성합니다.
     *
     * @param commentAuthorNickname 댓글 작성자의 닉네임
     * @param post                  댓글이 달린 게시글
     * @return 알람 내용 메시지
     */
    private String getCommentAlarmContent(String commentAuthorNickname, Post post) {
        String title = post.getTitle().length() > 20
                ? post.getTitle().substring(0, 17) + "..."
                : post.getTitle();
        return commentAuthorNickname + "님이 회원님의 \"" + title + "\"에 댓글을 달았습니다.";
    }

    /**
     * 답글 알람의 내용 메시지를 생성합니다.
     *
     * @param replyAuthorNickname 답글 작성자의 닉네임
     * @param parentComment       답글이 달린 원댓글
     * @return 알람 내용 메시지
     */
    private String getReplyAlarmContent(String replyAuthorNickname, Comment parentComment) {
        String content = parentComment.getContent().length() > 20
                ? parentComment.getContent().substring(0, 17) + "..."
                : parentComment.getContent();
        return replyAuthorNickname + "님이 회원님의 \"" + content + "\"에 답글을 달았습니다.";
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
