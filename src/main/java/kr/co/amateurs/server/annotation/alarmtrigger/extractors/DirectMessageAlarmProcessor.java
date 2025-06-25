package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageResponse;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import kr.co.amateurs.server.domain.entity.alarm.metadata.DirectMessageMetaData;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageAlarmProcessor implements AlarmProcessor {

    private final DirectMessageService directMessageService;

    @Override
    public long extractTargetUserId(JoinPoint joinPoint, Object result) {
        if (!(result instanceof DirectMessageResponse response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        DirectMessageRoom room = directMessageService.findRoomById(response.roomId());
        return room.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(response.senderId()))
                .findFirst()
                .map(Participant::getUserId)
                .orElseThrow(ErrorCode.USER_NOT_FOUND);
    }

    @Override
    public AlarmReceiver getReceiver() {
        return AlarmReceiver.DM_RECEIVER;
    }

    @Override
    public String buildContent(Object result) {
        if (!(result instanceof DirectMessageResponse response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }
        return response.senderNickname() + "님이 DM을 보냈습니다.";
    }

    @Override
    public AlarmMetaData buildMetaData(Object result) {
        if (!(result instanceof DirectMessageResponse response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }
        return new DirectMessageMetaData(response.roomId(), response.id());
    }
}
