package kr.co.amateurs.server.domain.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

import java.time.LocalDateTime;

public record CommentResponseDTO(
        @Schema(description = "댓글 ID", example = "1")
        Long id,

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "블라인드 여부", example = "false")
        boolean isBlinded,

        @Schema(description = "작성자 닉네임", example = "testUser")
        String nickname,

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg", nullable = true)
        String profileImageUrl,

        @Schema(description = "작성자 데브코스명", example = "AIBE", nullable = true)
        DevCourseTrack devCourseTrack,

        @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "5", nullable = true)
        Long parentCommentId,

        @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
        String content,

        @Schema(description = "대댓글 수", example = "3")
        int replyCount,

        @Schema(description = "좋아요 수", example = "7")
        int likeCount,

        @Schema(description = "좋아요 여부", example = "false")
        boolean hasLiked,

        @Schema(description = "댓글 작성 시간", example = "2025-06-27T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "댓글 수정 시간", example = "2025-06-27T14:35:00")
        LocalDateTime updatedAt
)
{
    public static CommentResponseDTO from(Comment comment, int replyCount, boolean hasLiked) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getPostId(),
                false,
                comment.getUser().getNickname(),
                comment.getUser().getImageUrl(),
                comment.getUser().getDevcourseName(),
                comment.getParentCommentId(),
                comment.getContent(),
                replyCount,
                comment.getLikeCount(),
                hasLiked,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public CommentResponseDTO applyBlindFilter() {
        if (isBlinded) {
            String blindedContent = "⚠️ 블라인드 처리된 댓글입니다";
            return new CommentResponseDTO(
                    this.id,
                    this.postId,
                    this.isBlinded,
                    this.nickname,
                    this.profileImageUrl,
                    this.devCourseTrack,
                    this.parentCommentId,
                    blindedContent,
                    this.replyCount,
                    this.likeCount,
                    this.hasLiked,
                    this.createdAt,
                    this.updatedAt
            );
        }
        return this;
    }
}
