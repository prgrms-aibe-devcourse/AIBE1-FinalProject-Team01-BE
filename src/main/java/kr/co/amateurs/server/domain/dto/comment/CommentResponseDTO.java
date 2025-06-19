package kr.co.amateurs.server.domain.dto.comment;

import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponseDTO(
    Long id,
    Long postId,
    String nickname,
    String profileImageUrl,
    Long parentCommentId,
    String content,
    int replyCount,
    int likeCount,
    boolean hasLiked,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
)
{
    public static CommentResponseDTO from(Comment comment, int replyCount, boolean hasLiked) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getPost().getId(),
                comment.getUser().getNickname(),
                comment.getUser().getImageUrl(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getContent(),
                replyCount,
                comment.getLikeCount(),
                hasLiked,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
