package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDto (
        @Schema(description = "JWT 액세스 토크")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "토큰 만료 시간(밀리초)", example = "360000")
        Long expiresIn
) {

    public static LoginResponseDto of(String accessToken, Long expiresIn) {
        return new LoginResponseDto(accessToken, "Bearer", expiresIn);
    }
}
