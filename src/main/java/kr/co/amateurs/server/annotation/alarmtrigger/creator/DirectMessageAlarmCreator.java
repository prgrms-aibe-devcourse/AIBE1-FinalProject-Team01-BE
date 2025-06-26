package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageResponse;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import kr.co.amateurs.server.domain.entity.alarm.metadata.DirectMessageMetaData;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.alarm.AlarmService;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageAlarmCreator implements AlarmCreator {
    private final AlarmService alarmService;
    private final DirectMessageService directMessageService;

    @Override
    public void createAlarm(Object result) {
        if (!(result instanceof DirectMessageResponse response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        DirectMessageRoom room = directMessageService.findRoomById(response.roomId());
        long userId = room.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(response.senderId()))
                .findFirst()
                .map(Participant::getUserId)
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        Alarm alarm = Alarm.builder()
                .userId(userId)
                .type(AlarmType.DIRECT_MESSAGE)
                .title(AlarmType.DIRECT_MESSAGE.getTitle())
                .content(getContent(result))
                .metaData(getMetaData(result))
                .build();

        alarmService.saveAlarm(alarm);
    }

    @Override
    public AlarmType getType() {
        return AlarmType.DIRECT_MESSAGE;
    }

    public String getContent(Object result) {
        if (!(result instanceof DirectMessageResponse response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }

        return switch (response.messageType()) {
            case TEXT -> response.senderNickname() + "님으로부터 새로운 메시지가 도착했습니다.";
            case IMAGE -> response.senderNickname() + "님이 이미지를 보냈습니다.";
            case FILE -> response.senderNickname() + "님이 파일을 보냈습니다.";
        };
    }

    public AlarmMetaData getMetaData(Object result) {
        if (!(result instanceof DirectMessageResponse response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }
        return new DirectMessageMetaData(response.roomId(), response.id());
    }
}
