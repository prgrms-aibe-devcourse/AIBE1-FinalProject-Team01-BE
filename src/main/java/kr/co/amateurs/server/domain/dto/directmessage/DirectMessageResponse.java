package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;

import java.time.LocalDateTime;

public record DirectMessageResponse(
        String id,
        String content,
        Long senderId,
        String senderName,
        MessageType messageType,
        LocalDateTime sentAt
) {
    public static DirectMessageResponse fromCollection(DirectMessage message) {
        return new DirectMessageResponse(
                message.getId(),
                message.getContent(),
                message.getSenderId(),
                message.getSenderNickname(),
                message.getMessageType(),
                message.getSentAt()
        );
    }
}
