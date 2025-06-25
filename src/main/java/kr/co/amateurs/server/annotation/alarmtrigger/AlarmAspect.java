package kr.co.amateurs.server.annotation.alarmtrigger;

import kr.co.amateurs.server.annotation.alarmtrigger.extractors.AlarmProcesser;
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

@Aspect
@Component
@RequiredArgsConstructor
public class AlarmAspect {

    private final AlarmRepository alarmRepository;
    private final AlarmProcessorRegistry alarmProcessorRegistry;

    @AfterReturning(pointcut = "@annotation(alarmTrigger)", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAlarm(JoinPoint joinPoint, AlarmTrigger alarmTrigger, Object result) {
        AlarmProcesser processer = alarmProcessorRegistry.getExtractor(alarmTrigger.receiver());
        long userId = processer.extractTargetUserId(joinPoint, result);
        String content = processer.buildContent(result);

        Alarm alarm = Alarm.builder()
                .userId(userId)
                .type(alarmTrigger.type())
                .title(alarmTrigger.type().getTitle())
                .content(content)
                .metaData(processer.buildMetaData(result))
                .build();

        alarmRepository.save(alarm);
    }
}
