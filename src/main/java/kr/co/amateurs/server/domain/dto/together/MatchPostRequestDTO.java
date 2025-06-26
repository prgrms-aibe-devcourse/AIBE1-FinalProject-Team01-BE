package kr.co.amateurs.server.domain.dto.together;

import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;

public record MatchPostRequestDTO(
        @NotNull String title,
        @NotNull String content,
        String tags,
        @NotNull MatchingType matchingType,
        @NotNull MatchingStatus status,
        String expertiseArea
) {}