package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserProfileEditRequestDto(

    @Schema(description = "현재 비밀번호 (비밀번호 변경 시에만 필수)", example = "password123")
    String currentPassword,

    @Schema(description = "새 닉네임", example = "newnick")
    @Size(min = 2, max = 10, message = "닉네임은 2-10자 사이여야 합니다")
    String nickname,

    @Schema(description = "새 이름", example = "최개발")
    @Size(min = 2, max = 5)
    String name,

    @Schema(description = "새 비밀번호", example = "newPassword123")
    @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다")
    String newPassword,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String imageUrl,

    @Schema(description = "관심 토픽 목록", example = "[\"FRONTEND\", \"BACKEND\"]")
    @Size(min = 1, max = 3, message = "토픽은 1개 이상 3개 이하로 선택해주세요")
    Set<Topic> topics
){
}
