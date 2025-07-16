package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import lombok.Builder;

@Builder
public record DirectMessageRequest(
        String content,
        Long senderId,
        String senderName,
        String senderProfileImage,
        MessageType messageType
) {
    public DirectMessage toCollection(String roomId) {
        return DirectMessage.builder()
                .content(content)
                .senderId(senderId)
                .senderNickname(senderName)
                .senderProfileImage(senderProfileImage)
                .roomId(roomId)
                .messageType(messageType)
                .build();
    }
}
