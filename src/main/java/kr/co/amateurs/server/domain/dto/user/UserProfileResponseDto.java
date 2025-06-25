package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import lombok.Builder;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record UserProfileResponseDto(
        @Schema(description = "사용자 ID")
        Long userId,

        @Schema(description = "이메일")
        String email,

        @Schema(description = "닉네임")
        String nickname,

        @Schema(description = "이름")
        String name,

        @Schema(description = "프로필 이미지 URL")
        String imageUrl,

        @Schema(description = "데브코스 이름")
        String devcourseName,

        @Schema(description = "데브코스 기수")
        String devcourseBatch,

        @Schema(description = "로그인 타입")
        ProviderType providerType,

        @Schema(description = "관심 토픽 목록")
        Set<Topic> topics
) {

    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .imageUrl(user.getImageUrl())
                .devcourseName(user.getDevcourseName())
                .devcourseBatch(user.getDevcourseBatch())
                .providerType(user.getProviderType())
                .topics(user.getUserTopics().stream()
                        .map(UserTopic::getTopic)
                        .collect(Collectors.toSet()))
                .build();
    }
}
