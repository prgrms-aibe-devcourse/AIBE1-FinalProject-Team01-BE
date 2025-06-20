package kr.co.amateurs.server.domain.dto.comment;

public record CommentRequestDTO(
    Long parentCommentId,
    String content
) {}
