package kr.co.amateurs.server.domain.dto.bookmark;

import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.springframework.data.domain.Page;

public record BookmarkResponseDTO(
        Long postId
//        BoardType boardType,
//        String title,
//        String content,
//        Integer likeCount,
//        Integer ViewCount,
//        String tag


) {
    public static BookmarkResponseDTO convertToDTO(Bookmark bookmark) {
        return new BookmarkResponseDTO(bookmark.getId());
    }

    public static Page<BookmarkResponseDTO> convertToDTO(Page<Bookmark> bookmarks) {
        return bookmarks.map(BookmarkResponseDTO::convertToDTO);
    }
}
