package kr.co.amateurs.server.domain.dto.auth;

import kr.co.amateurs.server.domain.entity.user.User;

import java.time.LocalDateTime;

public record SignupResponseDto(
        Long userId,
        String email,
        String nickname,
        LocalDateTime createdAt
) {

    public static SignupResponseDto fromEntity(User user) {
        return new SignupResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
