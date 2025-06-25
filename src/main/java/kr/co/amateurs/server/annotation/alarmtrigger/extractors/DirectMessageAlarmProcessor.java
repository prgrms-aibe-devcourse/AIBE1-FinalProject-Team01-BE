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

/**
 * 다이렉트 메시지 관련 알람을 처리하는 프로세서입니다.
 * 
 * 사용자가 DM을 전송했을 때, 해당 채팅방의 상대방에게
 * 알람을 보내는 기능을 담당합니다.
 * 
 * 처리 대상: DirectMessageResponse 타입의 결과 객체
 * 수신자: DM 수신자 (DIRECT_MESSAGE_RECEIVER)
 * 지원 메시지 타입: TEXT, IMAGE, FILE
 */
@Component
@RequiredArgsConstructor
public class DirectMessageAlarmProcessor implements AlarmProcessor {

    private final DirectMessageService directMessageService;

    /**
     * DM 채팅방에서 발신자가 아닌 상대방(수신자)의 ID를 추출합니다.
     * 
     * 현재는 1:1 채팅방만 지원하며, 채팅방 참가자 중에서 
     * 메시지 발신자가 아닌 사용자를 찾아 반환합니다.
     * 
     * @param joinPoint AOP 조인포인트 정보 (현재 미사용)
     * @param result DirectMessageResponse 타입의 메시지 응답 객체
     * @return 메시지를 받을 사용자의 ID
     * @throws CustomException result가 DirectMessageResponse가 아니거나 수신자를 찾을 수 없는 경우
     */
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

    /**
     * 이 프로세서가 처리하는 수신자 타입을 반환합니다.
     * 
     * @return DIRECT_MESSAGE_RECEIVER (DM 수신자)
     */
    @Override
    public AlarmReceiver getReceiver() {
        return AlarmReceiver.DM_RECEIVER;
    }

    /**
     * DM 알람 메시지를 생성합니다.
     * 
     * 메시지 타입에 따라 다른 내용을 생성합니다:
     * - TEXT: "{발신자닉네임}님으로부터 새로운 메시지가 도착했습니다."
     * - IMAGE: "{발신자닉네임}님이 이미지를 보냈습니다."
     * - FILE: "{발신자닉네임}님이 파일을 보냈습니다."
     * 
     * @param result DirectMessageResponse 타입의 메시지 응답 객체
     * @return 메시지 타입에 맞는 알람 메시지
     * @throws CustomException result가 DirectMessageResponse가 아닌 경우
     */
    @Override
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

    /**
     * DM 알람의 메타데이터를 생성합니다.
     * 
     * 알람을 클릭했을 때 해당 채팅방과 메시지로 이동할 수 있도록
     * 채팅방 ID와 메시지 ID를 포함한 메타데이터를 생성합니다.
     * 
     * @param result DirectMessageResponse 타입의 메시지 응답 객체
     * @return 채팅방 ID와 메시지 ID를 포함한 DirectMessageMetaData
     * @throws CustomException result가 DirectMessageResponse가 아닌 경우
     */
    @Override
    public AlarmMetaData getMetaData(Object result) {
        if (!(result instanceof DirectMessageResponse response)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_RESULT_TYPE);
        }
        return new DirectMessageMetaData(response.roomId(), response.id());
    }
}
