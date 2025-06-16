package kr.co.amateurs.server.domain.entity.comment;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Comment> replies = new ArrayList<>();

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private final Boolean isDeleted = false;
}
