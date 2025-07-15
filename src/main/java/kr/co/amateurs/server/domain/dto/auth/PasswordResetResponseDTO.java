package kr.co.amateurs.server.domain.dto.auth;

import lombok.Builder;

@Builder
public record PasswordResetResponseDTO(
        String message,
        String resetToken
) {
    public static PasswordResetResponseDTO success(String token) {
        return PasswordResetResponseDTO.builder()
                .message("비밀번호 재설정 정보가 확인되었습니다. 새로운 비밀번호를 설정해주세요.")
                .resetToken(token)
                .build();
    }
}