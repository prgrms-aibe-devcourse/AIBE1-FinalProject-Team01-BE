package kr.co.amateurs.server.controller.bookmark;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    @GetMapping("/users/{userId}/bookmarks")
    public ResponseEntity<PageResponseDTO<BookmarkResponseDTO>> getBookmarkPostList(
            @PathVariable Long userId,
            @ParameterObject @Valid PaginationParam paginationParam
            ) {
        PageResponseDTO<BookmarkResponseDTO> bookmarkList = bookmarkService.getBookmarkPostList(userId, paginationParam);
        return ResponseEntity.ok(bookmarkList);

    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    @PostMapping("/users/{userId}/bookmarks/{postId}")
    public ResponseEntity<BookmarkResponseDTO> addBookmarkPost(
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
         BookmarkResponseDTO bookmarkPost = bookmarkService.addBookmarkPost(userId, postId);
         return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkPost);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    @DeleteMapping("/users/{userId}/bookmarks/{postId}")
    public ResponseEntity<Void> removeBookmarkPost(
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
        bookmarkService.removeBookmarkPost(userId, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}