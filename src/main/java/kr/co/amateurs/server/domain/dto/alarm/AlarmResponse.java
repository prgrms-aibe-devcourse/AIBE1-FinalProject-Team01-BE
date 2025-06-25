package kr.co.amateurs.server.domain.dto.alarm;

import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;

import java.time.LocalDateTime;

public record AlarmResponse(
        String id,
        AlarmType type,
        String content,
        boolean isRead,
        LocalDateTime sentAt
) {
    public static AlarmResponse from(Alarm alarm) {
        return new AlarmResponse(
                alarm.getId(),
                alarm.getType(),
                alarm.getContent(),
                alarm.isRead(),
                alarm.getSentAt()
        );
    }
}
