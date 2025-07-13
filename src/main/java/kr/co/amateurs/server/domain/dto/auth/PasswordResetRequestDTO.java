package kr.co.amateurs.server.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestDTO(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이어야 합니다")
        String email,

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하여야 합니다")
        String name,

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하여야 합니다")
        String nickname
) {
}