package kr.co.amateurs.server.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDto {

    private Long userId;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;
}
