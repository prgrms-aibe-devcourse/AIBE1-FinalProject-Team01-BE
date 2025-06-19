package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;

public record DirectMessageRequest(
        String content,
        String senderId,
        MessageType messageType
) {
}
