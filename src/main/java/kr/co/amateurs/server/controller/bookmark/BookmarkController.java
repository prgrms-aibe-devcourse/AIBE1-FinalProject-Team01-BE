package kr.co.amateurs.server.controller.bookmark;


import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @GetMapping("/users/{userId}/bookmarks")
    public ResponseEntity<Page<BookmarkResponseDTO>> getBookmarkPostList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") SortType sortType
            ) {
        Page<BookmarkResponseDTO> bookmarkList = bookmarkService.getBookmarkPostList(userId, page, size, sortType);
        return ResponseEntity.ok(bookmarkList);
    }

    @PostMapping("/users/{userId}/bookmarks/{postId}")
    public ResponseEntity<BookmarkResponseDTO> addBookmarkPost(
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
        BookmarkResponseDTO bookmarkPost = bookmarkService.addBookmarkPost(userId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkPost);
    }

    @DeleteMapping("/users/{userId}/bookmarks/{postId}")
    public ResponseEntity<Void> removeBookmarkPost(
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
        bookmarkService.removeBookmarkPost(userId, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}