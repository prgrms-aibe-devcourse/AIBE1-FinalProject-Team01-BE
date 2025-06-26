package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import org.aspectj.lang.JoinPoint;

/**
 * 알람 처리를 위한 프로세서 인터페이스입니다.
 * 
 * 각 알람 타입별로 구현체를 만들어 다음 기능을 제공합니다:
 * - 알람 수신자 ID 추출
 * - 알람 내용 생성  
 * - 메타데이터 생성
 * 
 * Strategy Pattern을 적용하여 알람 타입별 처리 로직을 분리했습니다.
 */
public interface AlarmProcessor {
    
    /**
     * 메서드 실행 결과로부터 알람을 받을 사용자 ID를 추출합니다.
     * 
     * @param joinPoint AOP 조인포인트 정보
     * @param result 메서드 실행 결과 객체
     * @return 알람을 받을 사용자의 ID
     * @throws kr.co.amateurs.server.exception.CustomException 지원하지 않는 결과 타입인 경우
     */
    long extractTargetUserId(JoinPoint joinPoint, Object result);

    /**
     * 이 프로세서가 처리할 수 있는 알람 수신자 타입을 반환합니다.
     * 
     * @return 지원하는 AlarmReceiver 타입
     */
    AlarmType getType();

    /**
     * 알람 메시지 내용을 생성합니다.
     * 
     * @param result 메서드 실행 결과 객체
     * @return 사용자에게 표시될 알람 메시지
     * @throws kr.co.amateurs.server.exception.CustomException 지원하지 않는 결과 타입인 경우
     */
    String getContent(Object result);

    /**
     * 알람과 관련된 메타데이터를 생성합니다.
     * 
     * 메타데이터는 알람을 클릭했을 때 이동할 페이지 정보 등을 포함합니다.
     * 
     * @param result 메서드 실행 결과 객체
     * @return 알람 관련 메타데이터
     * @throws kr.co.amateurs.server.exception.CustomException 지원하지 않는 결과 타입인 경우
     */
    AlarmMetaData getMetaData(Object result);
}
