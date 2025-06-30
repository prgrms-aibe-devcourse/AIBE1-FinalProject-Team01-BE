package kr.co.amateurs.server.domain.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDTO(
    Long parentCommentId,
    @NotBlank(message = "댓글에 빈 내용을 넣을 수 없습니다.")
    String content
) {}
