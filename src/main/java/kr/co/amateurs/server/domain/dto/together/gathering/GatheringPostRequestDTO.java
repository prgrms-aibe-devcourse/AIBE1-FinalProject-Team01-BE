package kr.co.amateurs.server.domain.dto.together.gathering;

import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

public record GatheringPostRequestDTO(
        Long userId,
        String title,
        String content,
        String tags,
        GatheringType gatheringType,
        Integer headCount,
        String place,
        String period,
        String schedule
) {}