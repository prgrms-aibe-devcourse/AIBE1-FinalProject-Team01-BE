package kr.co.amateurs.server.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmDTO(
        @NotBlank(message = "토큰은 필수입니다")
        String token,

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$",
                message = "비밀번호는 8자 이상이며 알파벳과 숫자를 포함해야 합니다")
        String newPassword,

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        String confirmPassword
) {
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}