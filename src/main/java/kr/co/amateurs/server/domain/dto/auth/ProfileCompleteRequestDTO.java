package kr.co.amateurs.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import lombok.Builder;

import java.util.Set;

@Builder
public record ProfileCompleteRequestDTO(
        @Schema(description = "닉네임", example = "testnick")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 15, message = "닉네임은 2-15자 사이여야 합니다")
        String nickname,

        @Schema(description = "이름", example = "김테스트")
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 10, message = "이름은 2-10자 사이여야 합니다")
        String name,

        @Schema(description = "관심 토픽 목록", example = "[\"FRONTEND\", \"BACKEND\"]")
        @NotEmpty(message = "토픽을 최소 1개 선택해주세요")
        @Size(min = 1, max = 3, message = "토픽은 1개 이상 3개 이하로 선택해주세요")
        Set<Topic> topics
) {
}
