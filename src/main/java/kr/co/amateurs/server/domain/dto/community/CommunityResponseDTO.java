package kr.co.amateurs.server.domain.dto.community;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;

import java.time.LocalDateTime;

public record CommunityResponseDTO(
        Long postId,
        String title,
        String content,
        String nickname,
        String profileImageUrl,
        BoardType boardType,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String tags,
        boolean hasImages,
        boolean hasLiked,
        boolean hasBookmarked
) {
    public static CommunityResponseDTO from(Post post, boolean hasLiked, boolean hasBookmarked) {
        return new CommunityResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getNickname(),
                post.getUser().getImageUrl(),
                post.getBoardType(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getComments().size(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getTags(),
                post.getPostImages() != null && !post.getPostImages().isEmpty(),
                hasLiked,
                hasBookmarked
        );
    }
}
