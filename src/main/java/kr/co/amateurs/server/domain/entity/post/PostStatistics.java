package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_statistics", indexes = {
        @Index(name = "idx_post_statistics_view_count", columnList = "view_count"),
        @Index(name = "idx_post_statistics_post_view", columnList = "post_id, view_count")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostStatistics {
    @Id
    @Column(name = "post_id")
    private Long postId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder.Default
    @Column(nullable = false)
    private Integer viewCount = 0;

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updateViewCount(int newViewCount) {
        this.viewCount = newViewCount;
    }

    public static PostStatistics from(Post post) {
        return PostStatistics.builder()
                .post(post)
                .build();
    }
}
