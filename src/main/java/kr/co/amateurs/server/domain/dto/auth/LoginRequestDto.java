package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequestDto (
    @Schema(description = "이메일", example = "test@test.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @Schema(description = "비밀번호", example = "abcd1234")
    @NotBlank(message = "비밀번호는 필수입니다")
    String password
) {
}
