package kr.co.amateurs.server.domain.entity.post;


import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "popular_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PopularPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private Double popularityScore;

    @Column(nullable = false)
    private LocalDate calculatedDate;

    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
    private Integer commentCount;
}