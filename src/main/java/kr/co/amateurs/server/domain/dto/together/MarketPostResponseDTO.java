package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MarketPostResponseDTO(
        Long postId,
        Long userId,
        String title,
        String content,
        String tags,
        Integer viewCount,
        Integer likeCount,
        MarketStatus status,
        Integer price,
        String place,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
