package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;

import java.time.LocalDateTime;
import java.util.Set;

public record SignupResponseDto(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "이메일", example = "test@test.com")
        String email,
        @Schema(description = "닉네임", example = "testnick")
        String nickname,
        @Schema(description = "이름", example = "김테스트")
        String name,
        @Schema(description = "선택한 관심 토픽 목록")
        Set<Topic> topics,
        @Schema(description = "가입일시", example = "2025-06-19T02:01:56.537")
        LocalDateTime createdAt
) {

    public static SignupResponseDto fromEntity(User user, Set<Topic> topics) {
        return new SignupResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                topics,
                user.getCreatedAt()
        );
    }
}
