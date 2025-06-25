package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDto (
        @Schema(description = "JWT 액세스 토큰")
        String accessToken,

        @Schema(description = "JWT 리프레시 토큰")
        String refreshToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "토큰 만료 시간(밀리초)", example = "3600000")
        Long expiresIn
) {

    public static LoginResponseDto of(String accessToken, String refreshToken, Long expiresIn) {
        return new LoginResponseDto(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
