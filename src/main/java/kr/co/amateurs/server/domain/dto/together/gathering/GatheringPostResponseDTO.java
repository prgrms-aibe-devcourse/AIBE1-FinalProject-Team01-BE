package kr.co.amateurs.server.domain.dto.together.gathering;

import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import lombok.Builder;

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
) {}