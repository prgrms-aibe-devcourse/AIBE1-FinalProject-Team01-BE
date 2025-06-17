package kr.co.amateurs.server.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8,message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 10, message = "닉네임은 2-10자 사이여야 합니다")
    private String nickname;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 5, message = "이름은 2-5자 사이여야 합니다")
    private String name;

}
