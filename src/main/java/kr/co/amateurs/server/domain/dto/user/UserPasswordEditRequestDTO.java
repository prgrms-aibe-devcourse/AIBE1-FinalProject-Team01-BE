package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserPasswordEditRequestDTO(

        @Schema(description = "현재 비밀번호", example = "currentPassword123")
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newPassword123")
        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$",
                message = "비밀번호는 8자 이상이며 알파벳과 숫자를 포함해야 합니다")
        String newPassword
) {
}
