package kr.co.amateurs.server.annotation.alarmtrigger;

import kr.co.amateurs.server.annotation.alarmtrigger.extractors.AlarmProcessor;
import kr.co.amateurs.server.annotation.alarmtrigger.extractors.AlarmProcessorRegistry;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.repository.alarm.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알람 트리거 AOP 처리를 담당하는 Aspect 클래스입니다.
 * 
 * @AlarmTrigger 어노테이션이 붙은 메서드가 성공적으로 실행된 후
 * 자동으로 알람을 생성하고 저장하는 기능을 제공합니다.
 * 
 * 주요 특징:
 * - AfterReturning 어드바이스로 메서드 정상 완료 후에만 실행
 * - REQUIRES_NEW 트랜잭션으로 메인 비즈니스 로직과 분리
 * - Strategy Pattern으로 알람 타입별 처리 로직 분리
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AlarmAspect {

    private final AlarmRepository alarmRepository;
    private final AlarmProcessorRegistry alarmProcessorRegistry;

    /**
     * @AlarmTrigger 어노테이션이 붙은 메서드 실행 후 알람을 생성합니다.
     * 
     * 메서드가 정상적으로 완료되면 다음 단계로 알람을 처리합니다:
     * 1. 어노테이션 정보에서 알람 타입과 수신자 타입 추출
     * 2. 해당 타입에 맞는 AlarmProcessor 조회
     * 3. 프로세서를 통해 수신자 ID, 메시지, 메타데이터 생성
     * 4. Alarm 엔티티 생성 및 저장
     * 
     * 트랜잭션 분리:
     * REQUIRES_NEW 속성으로 메인 비즈니스 로직의 트랜잭션과 분리하여
     * 알람 생성 실패가 메인 로직에 영향을 주지 않도록 합니다.
     * 
     * @param joinPoint AOP 조인포인트 (메서드 정보)
     * @param alarmTrigger 메서드에 붙은 @AlarmTrigger 어노테이션
     * @param result 메서드 실행 결과 객체
     */
    @AfterReturning(pointcut = "@annotation(alarmTrigger)", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAlarm(JoinPoint joinPoint, AlarmTrigger alarmTrigger, Object result) {
        AlarmProcessor processor = alarmProcessorRegistry.getProcessor(alarmTrigger.receiver());
        long userId = processor.extractTargetUserId(joinPoint, result);
        String content = processor.getContent(result);

        Alarm alarm = Alarm.builder()
                .userId(userId)
                .type(alarmTrigger.type())
                .title(alarmTrigger.type().getTitle())
                .content(content)
                .metaData(processor.getMetaData(result))
                .build();

        alarmRepository.save(alarm);
    }
}
