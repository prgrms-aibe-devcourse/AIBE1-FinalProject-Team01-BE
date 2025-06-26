package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;

public interface AlarmCreator {
    void createAlarm(Object result);

    AlarmType getType();
}
