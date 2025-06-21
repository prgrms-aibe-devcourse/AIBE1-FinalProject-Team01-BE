package kr.co.amateurs.server.domain.dto.together;


import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;

public record MarketPostRequestDTO(
        Long userId,
        String title,
        String content,
        String tags,
        MarketStatus status,
        Integer price,
        String place
) {
}
