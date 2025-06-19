package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
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
