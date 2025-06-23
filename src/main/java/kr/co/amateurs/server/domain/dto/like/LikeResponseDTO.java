package kr.co.amateurs.server.domain.dto.like;

import kr.co.amateurs.server.domain.entity.like.Like;

public record LikeResponseDTO(
        String targetType,
        Long postId
) {
    public static LikeResponseDTO convertToDTO(Like like, String targetType) {
        return new LikeResponseDTO(targetType, like.getPost().getId());
    }
}
