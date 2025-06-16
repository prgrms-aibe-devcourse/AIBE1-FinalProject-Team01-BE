package kr.co.amateurs.server.domain.entity.like;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Like extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
}
