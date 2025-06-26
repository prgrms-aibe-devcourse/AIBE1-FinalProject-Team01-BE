package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record UserProfileEditResponseDto(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "닉네임", example = "newnick")
        String nickname,

        @Schema(description = "이름", example = "최개발")
        String name,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String imageUrl,

        @Schema(description = "관심 토픽 목록", example = "[\"FRONTEND\", \"BACKEND\"]")
        Set<Topic> topics,

        @Schema(description = "수정 날짜", example = "2025-06-27T10:30:00")
        LocalDateTime updatedAt
) {
    public static UserProfileEditResponseDto from(User user) {
        return UserProfileEditResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .imageUrl(user.getImageUrl())
                .topics(user.getUserTopics().stream()
                        .map(userTopic -> userTopic.getTopic())
                        .collect(Collectors.toSet()))
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}