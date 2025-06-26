package kr.co.amateurs.server.domain.entity.directmessage;

import jakarta.persistence.Id;
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
    @Builder.Default
    private final List<Participant> participants = new ArrayList<>();

    public void updateLastMessage(String message) {
        this.lastMessage = message;
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
}
