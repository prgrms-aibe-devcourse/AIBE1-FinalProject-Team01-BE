package kr.co.amateurs.server.annotation.alarmtrigger;

import kr.co.amateurs.server.annotation.alarmtrigger.creator.AlarmCreatorRegistry;
import kr.co.amateurs.server.annotation.alarmtrigger.creator.AlarmCreator;
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
    private final AlarmCreatorRegistry alarmCreatorRegistry;

    @AfterReturning(pointcut = "@annotation(alarmTrigger)", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAlarm(JoinPoint joinPoint, AlarmTrigger alarmTrigger, Object result) {
        AlarmCreator creator = alarmCreatorRegistry.getCreator(alarmTrigger.type());
        creator.createAlarm(result);
    }
}
