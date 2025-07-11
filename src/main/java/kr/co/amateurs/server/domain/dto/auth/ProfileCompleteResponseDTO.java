package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ProfileCompleteResponseDTO(
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
        @Schema(description = "프로필 완성 여부", example = "true")
        boolean isProfileCompleted,
        @Schema(description = "프로필 완성일시", example = "2025-07-11T14:30:00")
        LocalDateTime updatedAt
) {

    public static ProfileCompleteResponseDTO fromEntity(User user) {
        Set<Topic> topics = user.getUserTopics().stream()
                .map(userTopic -> userTopic.getTopic())
                .collect(Collectors.toSet());

        return new ProfileCompleteResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                topics,
                user.isProfileCompleted(),
                user.getUpdatedAt()
        );
    }
}