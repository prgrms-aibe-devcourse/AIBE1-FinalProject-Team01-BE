package kr.co.amateurs.server.domain.entity.like;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import lombok.*;

@Entity
@Table(name = "post_like", indexes = {
        @Index(name = "idx_like_post_id", columnList = "post_id"),
        @Index(name = "idx_like_user_id", columnList = "user_id"),
        @Index(name = "idx_like_post_user", columnList = "post_id, user_id"),
        @Index(name = "idx_like_comment_user", columnList = "comment_id, user_id"),
        @Index(name = "idx_like_post_comment", columnList = "post_id, comment_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Like extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @PrePersist
    @PreUpdate
    private void validateTable() {
        if ((post == null && comment == null) || (post != null && comment != null)) {
            throw ErrorCode.INVALID_LIKE.get();
        }
    }
}
