package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;


@Builder
public record GatheringPostResponseDTO(
        Long postId,
        String nickname,
        String devcourseName,
        String devcourseBatch,
        String userProfileImg,
        String title,
        String content,
        String tags,
        Integer viewCount,
        Integer likeCount,
        GatheringType gatheringType,
        GatheringStatus status,
        Integer headCount,
        String place,
        String period,
        String schedule,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean hasImages,
        boolean hasLiked,
        boolean hasBookmarked
) {
    public static GatheringPostResponseDTO convertToDTO(GatheringPost gp, Post post, boolean hasLiked, boolean hasBookmarked) {
        return new GatheringPostResponseDTO(
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
                gp.getGatheringType(),
                gp.getStatus(),
                gp.getHeadCount(),
                gp.getPlace(),
                gp.getPeriod(),
                gp.getSchedule(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPostImages() != null && !post.getPostImages().isEmpty(),
                hasLiked,
                hasBookmarked
        );
    }
}