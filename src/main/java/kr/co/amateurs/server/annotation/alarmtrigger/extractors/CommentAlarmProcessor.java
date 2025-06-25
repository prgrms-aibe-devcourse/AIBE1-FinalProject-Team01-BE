package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import kr.co.amateurs.server.domain.entity.alarm.metadata.CommentMetaData;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentAlarmProcessor implements AlarmProcesser {

    private final CommunityPostService postService;

    @Override
    public long extractTargetUserId(JoinPoint joinPoint, Object result) {
        if (!(result instanceof CommentResponseDTO responseDto)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }
        return postService.findById(responseDto.postId()).getUser().getId();
    }

    @Override
    public AlarmReceiver getReceiver() {
        return AlarmReceiver.POST_AUTHOR;
    }

    @Override
    public String buildContent(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        Post post = postService.findById(response.postId());
        String title = post.getTitle().length() > 20
                ? post.getTitle().substring(0, 17) + "..."
                : post.getTitle();
        return response.nickname() + "님이 작성하신 \"" + title + "\"에 댓글을 달았습니다.";
    }

    @Override
    public AlarmMetaData buildMetaData(Object result) {
        if (!(result instanceof CommentResponseDTO response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        return new CommentMetaData(response.postId(), response.id());
    }
}
