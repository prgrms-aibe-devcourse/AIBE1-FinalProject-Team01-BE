package kr.co.amateurs.server.domain.dto.bookmark;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;

import java.time.LocalDateTime;

public record PostBookmarkDTO(
        Long postId,
        BoardType boardType,
        String title,
        String content,
        Integer likeCount,
        Integer viewCount,
        String tag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements BookmarkResponseDTO {
    public static PostBookmarkDTO convertToDTO(Post post) {
        return new PostBookmarkDTO(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                post.getViewCount(),
                post.getTags(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
