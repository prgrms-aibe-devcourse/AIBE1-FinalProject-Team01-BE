package kr.co.amateurs.server.domain.entity.alarm.metadata;

public record DirectMessageMetaData(
        Long roomId,
        Long messageId
) implements AlarmMetaData {
}
