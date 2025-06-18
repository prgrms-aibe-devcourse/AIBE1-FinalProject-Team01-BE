package kr.co.amateurs.server.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;

import java.util.List;
import java.util.Set;


public record SignupRequestDto(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        String password,

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 10, message = "닉네임은 2-10자 사이여야 합니다")
        String nickname,

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 5, message = "이름은 2-5자 사이여야 합니다")
        String name,

        @NotEmpty(message = "토픽을 최소 1개 선택해주세요")
        Set<Topic> topics
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private String password;
        private String nickname;
        private String name;
        private Set<Topic> topics;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder topics(Set<Topic> topics) {
            this.topics = topics;
            return this;
        }

        public SignupRequestDto build() {
            return new SignupRequestDto(email, password, nickname, name, topics);
        }
    }
}
