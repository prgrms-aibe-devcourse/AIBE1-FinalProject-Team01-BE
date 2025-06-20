package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import lombok.Builder;

@Builder
public record DirectMessageRequest(
        String content,
        Long senderId,
        String senderName,
        MessageType messageType
) {
    public DirectMessage toCollection(String roomId) {
        return DirectMessage.builder()
                .content(content)
                .senderId(senderId)
                .senderNickname(senderName)
                .roomId(roomId)
                .messageType(messageType)
                .build();
    }
}
