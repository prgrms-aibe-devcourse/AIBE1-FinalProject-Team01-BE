package kr.co.amateurs.server.domain.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;

public record FileResponseDTO(
        @Schema(description = "업로드 성공 여부", example = "true")
        Boolean success,
        @Schema(description = "업로드 실패 시 실패 메시지", example = "파일 삭제 중 오류 발생")
        String message,
        @Schema(description = "업로드 성공 시 이미지 URL", example = "test.net/post/test-img")
        String url
) {
}
