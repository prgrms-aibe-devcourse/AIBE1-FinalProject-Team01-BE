package kr.co.amateurs.server.domain.dto.together;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;

public record MarketPostRequestDTO(
        @Schema(description = "게시글 제목", example = "test 제목")
        @NotBlank String title,
        @Schema(description = "게시글 내용", example = "test 내용")
        @NotBlank String content,
        @Schema(description = "게시글 태그", example = "Spring Boot")
        String tags,
        @Schema(description = "장터 물품 판매 상태", example = "SELLING")
        @NotNull MarketStatus status,
        @Schema(description = "장터 물품 가격", example = "10000")
        @PositiveOrZero Integer price,
        @Schema(description = "물품 판매 지역", example = "서울")
        String place
) {
}
