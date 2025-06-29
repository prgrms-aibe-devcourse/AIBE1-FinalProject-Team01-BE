package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserBasicProfileEditResponseDto(

        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이름", example = "김개발")
        String name,

        @Schema(description = "닉네임", example = "kimcoding")
        String nickname,

        @Schema(description = "프로필 이미지 URL")
        String imageUrl,

        @Schema(description = "수정 날짜")
        LocalDateTime updatedAt
) {
    public static UserBasicProfileEditResponseDto from(User user) {
        return UserBasicProfileEditResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
