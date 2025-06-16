package kr.co.amateurs.server.domain.entity.dm;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dm_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DmRoom extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "dmRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DmRoomParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "dmRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DmMessage> messages = new ArrayList<>();

    public void addParticipant(DmRoomParticipant participant) {
        this.participants.add(participant);
    }

    public void addMessage(DmMessage message) {
        this.messages.add(message);
    }

    public DmMessage getLatestMessage() {
        return this.messages.stream()
                .max((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .orElse(null);
    }

    public boolean hasParticipant(Long userId) {
        return this.participants.stream()
                .anyMatch(participant -> participant.getUser().getId().equals(userId));
    }

    public int getParticipantCount() {
        return this.participants.size();
    }
}
