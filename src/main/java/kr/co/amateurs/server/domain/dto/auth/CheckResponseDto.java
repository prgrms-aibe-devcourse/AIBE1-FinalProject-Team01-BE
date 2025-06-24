package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckResponseDto(

        @Schema(description = "사용 가능 여부", example = "true")
        boolean available,

        @Schema(description = "응답 메세지", example = "사용 가능한 이메일 입니다")
        String message
) {
    public static CheckResponseDto available(String type) {
        return new CheckResponseDto(true, "사용 가능한 " + type + "입니다");
    }

    public static CheckResponseDto unavailable(String type) {
        return new CheckResponseDto(false, "이미 사용중인 " + type + "입니다");
    }
}
