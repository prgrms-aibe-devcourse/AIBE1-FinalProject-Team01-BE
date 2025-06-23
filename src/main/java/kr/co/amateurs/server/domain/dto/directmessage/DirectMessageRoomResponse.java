package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import lombok.Builder;

@Builder
public record DirectMessageRoomResponse(
        String roomId,
        Long otherUserId,
        String lastMessage
) {
    public static DirectMessageRoomResponse fromCollection(DirectMessageRoom room, Long currentUserId) {
        Long otherUserId = room.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(currentUserId))
                .findFirst()
                .map(Participant::getUserId)
                .orElseThrow(ErrorCode.NOT_FOUND_OTHER_USER);

        return new DirectMessageRoomResponse(
                room.getId(),
                otherUserId,
                room.getLastMessage()
        );
    }
}
