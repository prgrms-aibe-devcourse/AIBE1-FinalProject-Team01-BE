package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

@Entity
@Table(name = "community_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPost extends BaseEntity {
    @OneToOne
    @JoinColumn(nullable = false)
    private Post post;

    public static CommunityPost from(Post post) {
        return CommunityPost.builder()
                .post(post)
                .build();
    }
}
