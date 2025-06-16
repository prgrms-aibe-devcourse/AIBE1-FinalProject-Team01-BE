package kr.co.amateurs.server.domain.entity.dm;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

@Entity
@Table(name = "dm_room_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DmRoomParticipant extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private DmRoom dmRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
}
