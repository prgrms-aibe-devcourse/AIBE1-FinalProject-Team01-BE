package kr.co.amateurs.server.domain.dto.comment;

import java.util.List;

public record CommentPageDTO(
        List<CommentResponseDTO> comments,
        Long nextCursor,
        boolean hasNext
) {
}
