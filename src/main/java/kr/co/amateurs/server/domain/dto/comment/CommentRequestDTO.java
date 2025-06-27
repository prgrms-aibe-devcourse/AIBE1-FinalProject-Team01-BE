package kr.co.amateurs.server.domain.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CommentRequestDTO(
        @Schema(
                description = "부모 댓글 ID (대댓글인 경우만 필요)",
                example = "5",
                nullable = true
        )
    Long parentCommentId,
        @Schema(
                description = "댓글 내용",
                example = "좋은 글 감사합니다!",
                maxLength = 500
        )
        @NotBlank(message = "댓글은 빈 내용일 수 없습니다.")
    String content
) {}
