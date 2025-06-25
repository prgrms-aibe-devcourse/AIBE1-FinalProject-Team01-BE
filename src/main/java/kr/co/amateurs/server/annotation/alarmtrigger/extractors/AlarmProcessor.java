package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

import kr.co.amateurs.server.domain.entity.alarm.metadata.AlarmMetaData;
import org.aspectj.lang.JoinPoint;

public interface AlarmProcessor {
    long extractTargetUserId(JoinPoint joinPoint, Object result);

    AlarmReceiver getReceiver();

    String buildContent(Object result);

    AlarmMetaData buildMetaData(Object result);
}
