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
        Long userId,
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
        LocalDateTime updatedAt
) {
    public static GatheringPostResponseDTO convertToDTO(GatheringPost gp, Post post) {
        return new GatheringPostResponseDTO(
                post.getId(),
                post.getUser().getId(),
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
                post.getUpdatedAt()
        );
    }

    public static Page<GatheringPostResponseDTO> convertToDTO(Page<GatheringPost> gpPage) {
        return gpPage.map(gp -> {
            Post post = gp.getPost();
            return convertToDTO(gp, post);
        });
    }
}