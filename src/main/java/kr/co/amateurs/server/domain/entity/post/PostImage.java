package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import lombok.*;

@Entity
@Table(name = "post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostImage extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

}
