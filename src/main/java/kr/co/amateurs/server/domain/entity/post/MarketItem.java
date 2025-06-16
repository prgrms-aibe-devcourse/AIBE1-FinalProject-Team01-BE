package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import lombok.*;

@Entity
@Table(name = "market_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MarketItem extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @JoinColumn(nullable = false)
    private MarketStatus status;
}
