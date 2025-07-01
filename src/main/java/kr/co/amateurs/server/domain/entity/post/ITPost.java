package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

@Entity
@Table(name = "it_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ITPost extends BaseEntity {
    @OneToOne
    @JoinColumn(nullable = false)
    private Post post;

    public static ITPost from(Post post) {
        return ITPost.builder()
                .post(post)
                .build();
    }
}
