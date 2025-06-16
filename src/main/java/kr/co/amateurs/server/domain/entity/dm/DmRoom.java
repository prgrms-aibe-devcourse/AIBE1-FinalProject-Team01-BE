package kr.co.amateurs.server.domain.entity.dm;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dm_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DmRoom extends BaseEntity {

    @OneToMany(mappedBy = "dmRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DmRoomParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "dmRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DmMessage> messages = new ArrayList<>();
}
