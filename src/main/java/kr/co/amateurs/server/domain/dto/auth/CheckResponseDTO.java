package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckResponseDTO(

        @Schema(description = "사용 가능 여부", example = "true")
        boolean available,

        @Schema(description = "응답 메시지", example = "사용 가능한 이메일입니다")
        String message
) {
    public static CheckResponseDTO available(String type) {
        return new CheckResponseDTO(true, "사용 가능한 " + type + "입니다");
    }

    public static CheckResponseDTO unavailable(String type) {
        return new CheckResponseDTO(false, "이미 사용중인 " + type + "입니다");
    }
}
