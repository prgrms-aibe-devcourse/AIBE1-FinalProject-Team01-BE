package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

public record GatheringPostRequestDTO(
        Long userId,
        String title,
        String content,
        String tags,
        GatheringType gatheringType,
        GatheringStatus status,
        Integer headCount,
        String place,
        String period,
        String schedule
) {}