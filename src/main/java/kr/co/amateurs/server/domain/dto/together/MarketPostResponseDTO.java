package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Builder
public record MarketPostResponseDTO(
        Long postId,
        String nickname,
        DevCourseTrack devcourseName,
        String devcourseBatch,
        String userProfileImg,
        String title,
        String content,
        String tags,
        Integer viewCount,
        Integer likeCount,
        MarketStatus status,
        Integer price,
        String place,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean hasImages,
        boolean hasLiked,
        boolean hasBookmarked
) {
    public static MarketPostResponseDTO convertToDTO(MarketItem mi, Post post, boolean hasLiked, boolean hasBookmarked){
        return new MarketPostResponseDTO(
                post.getId(),
                post.getUser().getNickname(),
                post.getUser().getDevcourseName(),
                post.getUser().getDevcourseBatch(),
                post.getUser().getImageUrl(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                mi.getStatus(),
                mi.getPrice(),
                mi.getPlace(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPostImages() != null && !post.getPostImages().isEmpty(),
                hasLiked,
                hasBookmarked
        );
    }
}
