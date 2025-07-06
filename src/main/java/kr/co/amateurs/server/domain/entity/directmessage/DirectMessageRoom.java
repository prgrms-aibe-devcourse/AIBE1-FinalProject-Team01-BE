package kr.co.amateurs.server.domain.entity.directmessage;

import jakarta.persistence.Id;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "direct_message_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DirectMessageRoom {
    @Id
    private String id;
    private String lastMessage;
    private LocalDateTime sentAt;
    private List<Participant> participants = new ArrayList<>();

    public void updateLastMessage(String message) {
        this.lastMessage = message;
        this.sentAt = LocalDateTime.now();
    }

    public void userLeaveRoom(Long userId) {
        participants.stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .ifPresent(Participant::exitRoom);
    }

    public Boolean allParticipantsLeft() {
        return participants.stream()
                .noneMatch(Participant::getIsActive);
    }

    public Boolean isParticipate(Long userId) {
        return participants.stream()
                .anyMatch(participant -> participant.getUserId().equals(userId));
    }

    public LocalDateTime getParticipantLeftAt(Long userId) {
        return participants.stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .map(Participant::getLeftAt)
                .orElse(null);
    }

    public static DirectMessageRoom from(List<User> users) {
        List<Participant> newParticipants = users.stream().map(Participant::from).toList();
        return DirectMessageRoom.builder()
                .participants(newParticipants)
                .build();
    }
}
