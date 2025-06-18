package kr.co.amateurs.server.domain.dto.together.match;

import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;

import java.time.LocalDateTime;

public record MatchPostResponseDTO(
        Long postId,
        Long userId,
        String title,
        String content,
        String tags,
        Integer viewCount,
        Integer likeCount,
        MatchingType matchingType,
        MatchingStatus status,
        String expertiseArea,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
