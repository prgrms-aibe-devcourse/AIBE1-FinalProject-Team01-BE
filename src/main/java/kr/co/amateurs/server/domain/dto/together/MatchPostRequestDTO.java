package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;

public record MatchPostRequestDTO(
        Long userId,
        String title,
        String content,
        String tags,
        MatchingType matchingType,
        MatchingStatus status,
        String expertiseArea
) {}