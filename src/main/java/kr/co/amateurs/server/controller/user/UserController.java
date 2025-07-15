package kr.co.amateurs.server.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.post.PostResponseDTO;
import kr.co.amateurs.server.domain.dto.user.*;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookmarkService bookmarkService;
    private final LikeService likeService;
    private final PostService postService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인 한 사용자의 프로필 정보를 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser() {
        UserProfileResponseDTO response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기본 정보 수정", description = "현재 로그인 한 사용자의 기본 프로필을 수정합니다")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserBasicProfileEditResponseDTO> updateBasicProfile(
            @Valid @RequestBody UserBasicProfileEditRequestDTO request) {
        UserBasicProfileEditResponseDTO response = userService.updateBasicProfile(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새로운 비밀번호로 변경합니다")
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPasswordEditResponseDTO> updatePassword(
            @Valid @RequestBody UserPasswordEditRequestDTO request) {
        UserPasswordEditResponseDTO response = userService.updatePassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "관심 주제 변경", description = "사용자의 관심 주제를 변경합니다")
    @PutMapping("/me/topics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserTopicsEditDTO> updateTopics(
            @Valid @RequestBody UserTopicsEditDTO request) {
        UserTopicsEditDTO response = userService.updateTopics(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 탈퇴합니다")
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDeleteResponseDTO> deleteUser(
            @RequestBody UserDeleteRequestDTO request) {
        UserDeleteResponseDTO response = userService.deleteUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/bookmarked")
    @Operation(summary = "북마크한 게시글 리스트", description = "북마크한 게시글 리스트를 가져옵니다.")
    public ResponseEntity<PageResponseDTO<PostResponseDTO>> getBookmarkPostList(
            @ParameterObject @Valid PaginationParam paginationParam
    ) {
        PageResponseDTO<PostResponseDTO> bookmarkList = bookmarkService.getBookmarkPostList(paginationParam);
        return ResponseEntity.ok(bookmarkList);
    }

    @GetMapping("/me/liked")
    @Operation(summary = "좋아요한 게시글 리스트", description = "좋아요한 게시글 리스트를 가져옵니다.")
    public ResponseEntity<PageResponseDTO<PostResponseDTO>> getLikePostList(
            @ParameterObject @Valid PaginationParam paginationParam
    ) {
        PageResponseDTO<PostResponseDTO> likePostList = likeService.getLikePostList(paginationParam);
        return ResponseEntity.ok(likePostList);
    }

    @GetMapping("/me/posts")
    @Operation(summary = "작성한 게시글 리스트", description = "작성한 게시글 리스트를 가져옵니다.")
    public ResponseEntity<PageResponseDTO<PostResponseDTO>> getMyPostList(
            @ParameterObject @Valid PaginationParam paginationParam
    ) {
        PageResponseDTO<PostResponseDTO> likePostList = postService.getMyPostList(paginationParam);
        return ResponseEntity.ok(likePostList);
    }

    @GetMapping("/{nickname}/info")
    @Operation(summary = "유저 모달 정보", description = "유저 정보 모달에 포함될 요약 정보를 가져옵니다.")
    public ResponseEntity<UserModalInfoResponseDTO> getUserModalInfo(
            @PathVariable String nickname){
        UserModalInfoResponseDTO response = userService.getUserModalInfo(nickname);
        return ResponseEntity.ok(response);
    }
}
