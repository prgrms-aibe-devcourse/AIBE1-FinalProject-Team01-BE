package kr.co.amateurs.server.annotation.alarmtrigger;

import kr.co.amateurs.server.annotation.alarmtrigger.extractors.AlarmReceiver;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AlarmTrigger {
    AlarmType type();

    AlarmReceiver receiver();
}
