package kr.co.amateurs.server.domain.dto.bookmark;

import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import lombok.Builder;

@Builder
public record BookmarkResponseDTO(
        Long userId,
        Long postId,
        String succeed
) {
    public static BookmarkResponseDTO addSucceed(Bookmark bookmark) {
        return BookmarkResponseDTO.builder()
                .postId(bookmark.getPost().getId())
                .userId(bookmark.getUser().getId())
                .succeed("북마크에 성공했습니다")
                .build();
    }
}
