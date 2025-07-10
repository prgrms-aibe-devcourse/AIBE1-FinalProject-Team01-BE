package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenReissueResponseDTO(
        @Schema(description = "새로 발급된 JWT 액세스 토큰")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "토큰 만료 시간(밀리초)", example = "3600000")
        Long expiresIn
) {
    public static TokenReissueResponseDTO of(String accessToken, Long expiresIn) {
        return new TokenReissueResponseDTO(accessToken, "Bearer", expiresIn);
    }
}
