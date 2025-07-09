package kr.co.amateurs.server.domain.dto.user;

import lombok.Builder;

@Builder
public record UserDeleteResponseDTO(
        String message
) {
    public static UserDeleteResponseDTO success() {
        return UserDeleteResponseDTO.builder()
                .message("회원 탈퇴가 성공적으로 처리되었습니다")
                .build();
    }
}
