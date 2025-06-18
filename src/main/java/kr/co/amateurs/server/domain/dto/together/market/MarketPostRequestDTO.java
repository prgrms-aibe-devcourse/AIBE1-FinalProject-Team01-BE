package kr.co.amateurs.server.domain.dto.together.market;


public record MarketPostRequestDTO(
        Long userId,
        String title,
        String content,
        String tags,
        Integer price,
        String place
) {
}
