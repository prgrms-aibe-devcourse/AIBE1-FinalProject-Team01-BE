package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequestDTO(
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        String refreshToken
) {
}
