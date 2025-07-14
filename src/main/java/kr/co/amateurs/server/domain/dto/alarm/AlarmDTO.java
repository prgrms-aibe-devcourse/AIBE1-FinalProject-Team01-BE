package kr.co.amateurs.server.domain.dto.alarm;

import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;

import java.time.LocalDateTime;

public record AlarmDTO(
        String id,
        AlarmType type,
        String title,
        String content,
        boolean isRead,
        LocalDateTime sentAt
) {
    public static AlarmDTO from(Alarm alarm) {
        return new AlarmDTO(
                alarm.getId(),
                alarm.getType(),
                alarm.getTitle(),
                alarm.getContent(),
                alarm.isRead(),
                alarm.getSentAt()
        );
    }
}
