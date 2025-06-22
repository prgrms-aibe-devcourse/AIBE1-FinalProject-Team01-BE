package kr.co.amateurs.server.domain.dto.directmessage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DirectMessageRoomExitRequest(
        @Schema(description = "방 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "방 ID는 필수입니다")
        String roomId,

        @Schema(description = "나갈 사용자 ID", example = "123")
        @NotNull(message = "사용자 ID는 필수입니다")
        Long userId
) {
}
