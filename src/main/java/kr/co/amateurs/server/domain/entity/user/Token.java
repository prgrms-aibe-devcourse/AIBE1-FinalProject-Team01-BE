package kr.co.amateurs.server.domain.entity.user;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Token extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;
}
