package kr.co.amateurs.server.domain.dto.auth;

import lombok.Builder;

@Builder
public record PasswordResetConfirmResponseDTO(
        String message
) {
    public static PasswordResetConfirmResponseDTO success() {
        return PasswordResetConfirmResponseDTO.builder()
                .message("비밀번호가 성공적으로 변경되었습니다")
                .build();
    }
}