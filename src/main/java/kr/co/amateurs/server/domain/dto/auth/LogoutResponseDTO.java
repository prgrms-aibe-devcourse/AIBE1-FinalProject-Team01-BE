package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LogoutResponseDTO(
        @Schema(description = "로그아웃 결과 메시지", example = "로그아웃이 성공적으로 완료되었습니다")
        String message
) {

    public static LogoutResponseDTO success() {
        return new LogoutResponseDTO("로그아웃이 성공적으로 완료되었습니다");
    }
}
