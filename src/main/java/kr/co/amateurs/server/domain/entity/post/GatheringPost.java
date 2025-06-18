package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import lombok.*;

@Entity
@Table(name = "gathering_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GatheringPost extends BaseEntity {

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
    private String period;
    private String schedule;

    public void update(GatheringPostRequestDTO dto){
        this.gatheringType = dto.gatheringType();
        this.status = dto.status();
        this.headCount = dto.headCount();
        this.place = dto.place();
        this.period = dto.period();
        this.schedule = dto.schedule();
    }
}
