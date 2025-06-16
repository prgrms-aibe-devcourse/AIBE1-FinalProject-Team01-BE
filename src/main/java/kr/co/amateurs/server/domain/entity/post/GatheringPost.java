package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import lombok.*;

@Entity
@Table(name = "gathering_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GatheringPost extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GatheringType gatheringType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GatheringStatus status;
    private Integer headCount;
    private String place;
    private Integer period;

    @Column(name = "required_skills")
    private String requiredSkills;
}
