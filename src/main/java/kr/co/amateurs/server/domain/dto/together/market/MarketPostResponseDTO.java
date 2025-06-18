package kr.co.amateurs.server.domain.dto.together.market;

import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;

import java.time.LocalDateTime;

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
