package kr.co.amateurs.server.domain.entity.alarm.metadata;

public record DirectMessageMetaData(
        String roomId,
        String messageId
) implements AlarmMetaData {
}
