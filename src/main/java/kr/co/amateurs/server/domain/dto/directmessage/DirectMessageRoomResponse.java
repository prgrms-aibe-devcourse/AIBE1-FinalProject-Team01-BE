package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DirectMessageRoomResponse(
        String roomId,
        Long otherUserId,
        String otherUserNickname,
        String otherUserProfileImage,
        String lastMessage,
        LocalDateTime sentAt
) {
    public static DirectMessageRoomResponse fromCollection(DirectMessageRoom room, User otherUser) {
        return new DirectMessageRoomResponse(
                room.getId(),
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getImageUrl(),
                room.getLastMessage(),
                room.getSentAt()
        );
    }
}
