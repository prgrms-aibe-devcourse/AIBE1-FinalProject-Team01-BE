package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DirectMessageRoomResponse(
        String id,
        Long partnerId,
        String partnerNickname,
        String partnerProfileImage,
        DevCourseTrack devcourseName,
        String devcourseBatch,
        String lastMessage,
        LocalDateTime sentAt
) {
    public static DirectMessageRoomResponse fromCollection(DirectMessageRoom room, User currentUser) {
        List<Participant> participants = room.getParticipants();
        Participant partner = participants.get(0).getUserId().equals(currentUser.getId())
                ? participants.get(1)
                : participants.get(0);

        return new DirectMessageRoomResponse(
                room.getId(),
                partner.getUserId(),
                partner.getNickname(),
                partner.getProfileImage(),
                partner.getDevcourseName(),
                partner.getDevcourseBatch(),
                room.getLastMessage(),
                room.getSentAt()
        );
    }
}
