package kr.co.amateurs.server.domain.entity.comment;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_post_parent_deleted",
                columnList = "post_id, parent_comment_id, is_deleted"),

        @Index(name = "idx_comment_parent_deleted",
                columnList = "parent_comment_id, is_deleted"),

        @Index(name = "idx_comment_created_at",
                columnList = "created_at"),

        @Index(name = "idx_comment_cursor_paging",
                columnList = "post_id, parent_comment_id, is_deleted, id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isBlinded = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer replyCount = 0;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public static Comment from(CommentRequestDTO requestDTO, Long postId, User user, Long parentCommentId) {
        return Comment.builder()
                .user(user)
                .postId(postId)
                .content(requestDTO.content())
                .parentCommentId(parentCommentId)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateBlinded(boolean isBlinded) {
        this.isBlinded = isBlinded;
    }
}
