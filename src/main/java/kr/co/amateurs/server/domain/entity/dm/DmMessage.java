package kr.co.amateurs.server.domain.entity.dm;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

@Entity
@Table(name = "dm_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DmMessage extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private DmRoom dmRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User sender;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public boolean isSentBy(User user) {
        return this.sender.equals(user);
    }

    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }
}
