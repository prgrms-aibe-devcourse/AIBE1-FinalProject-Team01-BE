package kr.co.amateurs.server.domain.entity.user;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import kr.co.amateurs.server.domain.entity.user.enums.VerificationStatus;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Verification extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;
}
