package kr.co.amateurs.server.domain.dto.bookmark;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

import java.time.LocalDateTime;
import java.util.List;

import static kr.co.amateurs.server.domain.entity.post.Post.convertTagToList;

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
            @Schema(description = "팀원 모집 종류", example = "STUDY")
            GatheringType gatheringType,
            @Schema(description = "팀원 모집 상태", example = "RECRUITING")
            GatheringStatus status,
            @Schema(description = "모집 인원", example = "4")
            int headCount,
            @Schema(description = "모임 장소", example = "서울")
            String place,
            @Schema(description = "모임 기간", example = "250625 ~ 250627")
            String period,
            @Schema(description = "모임 일정", example = "매주 화, 목 저녁 7시")
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
