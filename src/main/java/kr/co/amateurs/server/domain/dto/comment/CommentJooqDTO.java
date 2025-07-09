package kr.co.amateurs.server.domain.dto.comment;

import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentJooqDTO(
        Long id,
        Long postId,
        String nickname,
        String profileImageUrl,
        DevCourseTrack devCourseTrack,
        Long parentCommentId,
        String content,
        Integer replyCount,
        Integer likeCount,
        Boolean hasLiked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public CommentJooqDTO withLiked(boolean liked) {
        return CommentJooqDTO.builder()
                .id(id)
                .postId(postId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .devCourseTrack(devCourseTrack)
                .parentCommentId(parentCommentId)
                .content(content)
                .replyCount(replyCount)
                .likeCount(likeCount)
                .hasLiked(liked)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
    public CommentResponseDTO toResponseDTO() {
        return new CommentResponseDTO(
                id,
                postId,
                nickname,
                profileImageUrl,
                devCourseTrack,
                parentCommentId,
                content,
                replyCount != null ? replyCount : 0,
                likeCount != null ? likeCount : 0,
                hasLiked != null ? hasLiked : false,
                createdAt,
                updatedAt
        );
    }
}
