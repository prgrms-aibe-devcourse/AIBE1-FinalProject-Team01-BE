package kr.co.amateurs.server.controller.bookmark;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookmark", description = "북마크 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    @GetMapping("/users/{userId}/bookmarks")
    @Operation(summary = "북마크한 게시글 리스트", description = "북마크한 게시글 리스트를 가져옵니다.")
    public ResponseEntity<PageResponseDTO<BookmarkResponseDTO>> getBookmarkPostList(
            @PathVariable Long userId,
            @ParameterObject @Valid PaginationParam paginationParam
            ) {
        PageResponseDTO<BookmarkResponseDTO> bookmarkList = bookmarkService.getBookmarkPostList(userId, paginationParam);
        return ResponseEntity.ok(bookmarkList);

    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    @PostMapping("/users/{userId}/bookmarks/{postId}")
    @Operation(summary = "북마크 등록", description = "특정 게시글을 북마크에 등록합니다.")
    public ResponseEntity<BookmarkResponseDTO> addBookmarkPost(
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
         BookmarkResponseDTO bookmarkPost = bookmarkService.addBookmarkPost(userId, postId);
         return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkPost);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    @DeleteMapping("/users/{userId}/bookmarks/{postId}")
    @Operation(summary = "북마크 제거", description = "북마크 해둔 특정 게시글의 북마크를 해제합니다.")
    public ResponseEntity<Void> removeBookmarkPost(
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
        bookmarkService.removeBookmarkPost(userId, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}