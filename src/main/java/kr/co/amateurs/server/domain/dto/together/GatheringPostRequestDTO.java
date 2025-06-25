package kr.co.amateurs.server.domain.dto.together;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

public record GatheringPostRequestDTO(
        @NotNull String title,
        @NotNull String content,
        String tags,
        @NotNull GatheringType gatheringType,
        @NotNull GatheringStatus status,
        @PositiveOrZero Integer headCount,
        String place,
        String period,
        String schedule
) {}