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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @GetMapping("/users/{userId}/bookmarks")
    public ResponseEntity<PageResponseDTO<BookmarkResponseDTO>> getBookmarkPostList(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull Long userId,
            @ModelAttribute @Valid PaginationParam paginationParam
            ) {
        // 본인 북마크 리스트만 조회 가능하게 함
        if(currentUser.getUser().getRole().equals(Role.ADMIN) || currentUser.getUser().getId().equals(userId)) {
            PageResponseDTO<BookmarkResponseDTO> bookmarkList = bookmarkService.getBookmarkPostList(userId, paginationParam);
            return ResponseEntity.ok(bookmarkList);
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

    }

    @PostMapping("/users/{userId}/bookmarks/{postId}")
    public ResponseEntity<BookmarkResponseDTO> addBookmarkPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
        // 본인 북마크 리스트에만 추가 가능하게 함
        if(currentUser.getUser().getRole().equals(Role.ADMIN) || currentUser.getUser().getId().equals(userId)) {
            BookmarkResponseDTO bookmarkPost = bookmarkService.addBookmarkPost(userId, postId);
            return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkPost);
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    @DeleteMapping("/users/{userId}/bookmarks/{postId}")
    public ResponseEntity<Void> removeBookmarkPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long userId,
            @PathVariable Long postId
    ){
        //본인 북마크 항목만 제거 가능하도록 함
        if(currentUser.getUser().getRole().equals(Role.ADMIN) || currentUser.getUser().getId().equals(userId)) {
            bookmarkService.removeBookmarkPost(userId, postId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

    }
}