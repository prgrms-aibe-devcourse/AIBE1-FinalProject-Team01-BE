package kr.co.amateurs.server.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UserPasswordEditRequestDTO(

        @Schema(description = "현재 비밀번호", example = "currentPassword123")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newPassword123")
        String newPassword
) {
}
