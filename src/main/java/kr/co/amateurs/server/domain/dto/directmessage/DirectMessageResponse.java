package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;

public record DirectMessageResponse(
        String id,
        String content,
        String senderId,
        MessageType messageType
) {
}
