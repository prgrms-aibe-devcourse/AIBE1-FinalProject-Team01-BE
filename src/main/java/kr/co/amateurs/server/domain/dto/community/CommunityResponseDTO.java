package kr.co.amateurs.server.domain.dto.community;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;

import java.time.LocalDateTime;

public record CommunityResponseDTO(
        Long postId,
        String title,
        String content,
        String authorName,
        BoardType boardType,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String tags,
        String thumbnailImage,
        boolean hasImages,
        boolean hasLiked
) {
    public static CommunityResponseDTO from(Post post, int commentCount, String thumbnailImage, boolean hasImages, boolean hasLiked) {
        return new CommunityResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getNickname(),
                post.getBoardType(),
                post.getViewCount(),
                post.getLikeCount(),
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getTags(),
                thumbnailImage,
                hasImages,
                hasLiked
        );
    }
}
