package kr.co.amateurs.server.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmDTO(
        @NotBlank(message = "토큰은 필수입니다")
        String token,

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "비밀번호는 8자이상,  합니다")
        String newPassword,

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        String confirmPassword
) {
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}