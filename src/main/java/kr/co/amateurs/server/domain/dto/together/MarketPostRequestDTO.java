package kr.co.amateurs.server.domain.dto.together;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;

public record MarketPostRequestDTO(
        @NotNull String title,
        @NotNull String content,
        String tags,
        @NotNull MarketStatus status,
        @PositiveOrZero Integer price,
        String place
) {
}
