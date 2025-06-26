package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import kr.co.amateurs.server.domain.entity.alarm.metadata.CommentMetaData;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 * 댓글 관련 알람을 처리하는 프로세서입니다.
 * 
 * 사용자가 게시글에 댓글을 작성했을 때, 해당 게시글의 작성자에게
 * 알람을 보내는 기능을 담당합니다.
 * 
 * 처리 대상: CommentResponseDTO 타입의 결과 객체
 * 수신자: 게시글 작성자 (POST_AUTHOR)
 */
@Component
@RequiredArgsConstructor
public class CommentAlarmProcessor implements AlarmProcessor {

    private final CommunityPostService postService;

    /**
     * 댓글이 작성된 게시글의 작성자 ID를 추출합니다.
     * 
     * @param joinPoint AOP 조인포인트 정보 (현재 미사용)
     * @param result CommentResponseDTO 타입의 댓글 응답 객체
     * @return 게시글 작성자의 사용자 ID
     * @throws CustomException result가 CommentResponseDTO가 아닌 경우
     */
    @Override
    public long extractTargetUserId(JoinPoint joinPoint, Object result) {
        if (!(result instanceof CommentResponseDTO responseDto)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }
        //todo: post repo 조회 줄이기
        return postService.findById(responseDto.postId()).getUser().getId();
    }

    /**
     * 이 프로세서가 처리하는 알람타입을 반환합니다.
     * 
     * @return AlarmType.COMMENT
     */
    @Override
    public AlarmType getType() {
        return AlarmType.COMMENT;
    }

    /**
     * 댓글 알람 메시지를 생성합니다.
     * 
     * 메시지 형식: "{댓글작성자닉네임}님이 작성하신 \"{게시글제목}\"에 댓글을 달았습니다."
     * 게시글 제목이 20자를 초과하면 17자까지만 표시하고 "..." 추가
     * 
     * @param result CommentResponseDTO 타입의 댓글 응답 객체
     * @return 생성된 알람 메시지
     * @throws CustomException result가 CommentResponseDTO가 아닌 경우
     */
    @Override
    public String getContent(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        Post post = postService.findById(response.postId());
        String title = post.getTitle().length() > 20
                ? post.getTitle().substring(0, 17) + "..."
                : post.getTitle();
        return response.nickname() + "님이 작성하신 \"" + title + "\"에 댓글을 달았습니다.";
    }

    /**
     * 댓글 알람의 메타데이터를 생성합니다.
     * 
     * 알람을 클릭했을 때 해당 게시글과 댓글로 이동할 수 있도록
     * 게시글 ID와 댓글 ID를 포함한 메타데이터를 생성합니다.
     * 
     * @param result CommentResponseDTO 타입의 댓글 응답 객체
     * @return 게시글 ID와 댓글 ID를 포함한 CommentMetaData
     * @throws CustomException result가 CommentResponseDTO가 아닌 경우
     */
    @Override
    public AlarmMetaData getMetaData(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        return new CommentMetaData(response.postId(), response.id());
    }
}
