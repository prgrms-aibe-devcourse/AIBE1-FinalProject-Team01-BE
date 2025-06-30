package kr.co.amateurs.server.domain.dto.bookmark;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

import java.time.LocalDateTime;

public record GatheringBookmarkDTO(
        Long postId,
        BoardType boardType,
        String title,
        String content,
        Integer likeCount,
        Integer viewCount,
        String tag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        GatheringInfo gatheringInfo
) implements BookmarkResponseDTO {
    public record GatheringInfo(
            GatheringType gatheringType,
            GatheringStatus status,
            int headCount,
            String place,
            String period,
            String schedule
    ) {}

    public static GatheringBookmarkDTO convertToDTO(GatheringPost gp) {
        Post p = gp.getPost();
        GatheringInfo gi = new GatheringInfo(
                gp.getGatheringType(),
                gp.getStatus(),
                gp.getHeadCount(),
                gp.getPlace(),
                gp.getPeriod(),
                gp.getSchedule()
        );
        return new GatheringBookmarkDTO(
                p.getId(),
                p.getBoardType(),
                p.getTitle(),
                p.getContent(),
                p.getLikeCount(),
                p.getViewCount(),
                p.getTags(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                gi
        );
    }
}
