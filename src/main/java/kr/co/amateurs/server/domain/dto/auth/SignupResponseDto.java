package kr.co.amateurs.server.domain.dto.auth;

import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;

import java.time.LocalDateTime;
import java.util.Set;

public record SignupResponseDto(
        Long userId,
        String email,
        String nickname,
        String name,
        Set<Topic> topics,
        LocalDateTime createdAt
) {

    public static SignupResponseDto fromEntity(User user) {
        return new SignupResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getTopics(),
                user.getCreatedAt()
        );
    }
}
