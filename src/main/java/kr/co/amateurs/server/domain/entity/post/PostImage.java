package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

@Entity
@Table(name = "post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostImage extends BaseEntity {

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

}
