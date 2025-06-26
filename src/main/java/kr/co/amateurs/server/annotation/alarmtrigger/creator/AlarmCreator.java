package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;

/**
 * 알람 생성을 담당하는 인터페이스입니다.
 * 
 * Strategy Pattern을 적용하여 알람 타입별로 서로 다른 알람 생성 로직을 구현할 수 있습니다.
 * 각 구현체는 특정 알람 타입에 대한 알람 생성 책임을 가지며,
 * AlarmCreatorRegistry를 통해 타입별로 관리됩니다.
 * 
 * 구현 예시:
 * - CommentAlarmCreator: 댓글 알람 생성
 * - ReplyAlarmCreator: 대댓글 알람 생성  
 * - DirectMessageAlarmCreator: 직접 메시지 알람 생성
 */
public interface AlarmCreator {
    
    /**
     * 메서드 실행 결과를 기반으로 알람을 생성하고 저장합니다.
     * 
     * 각 구현체는 해당하는 결과 타입을 검증하고,
     * 적절한 수신자, 메시지 내용, 메타데이터를 추출하여 알람을 생성합니다.
     * 
     * @param result 알람 생성의 기반이 되는 메서드 실행 결과 객체
     * @throws CustomException 지원하지 않는 결과 타입이거나 필수 데이터가 누락된 경우
     */
    void createAlarm(Object result);

    /**
     * 이 알람 생성자가 처리하는 알람 타입을 반환합니다.
     * 
     * AlarmCreatorRegistry에서 이 값을 키로 사용하여
     * 알람 타입별 생성자를 매핑합니다.
     * 
     * @return 처리 가능한 알람 타입
     */
    AlarmType getType();
}
