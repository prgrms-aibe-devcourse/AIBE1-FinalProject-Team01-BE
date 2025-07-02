package kr.co.amateurs.server.controller.like;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="Like", description = "좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {
    private final LikeService likeService;

    @PreAuthorize("hasAnyRole('ADMIN', 'GUEST', 'STUDENT')")
    @PostMapping("/posts/{postId}/likes")
    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 누릅니다.")
    public ResponseEntity<LikeResponseDTO> addLikeToPost(
            @PathVariable Long postId
    ){
        LikeResponseDTO result = likeService.addLikeToPost(postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GUEST', 'STUDENT')")
    @PostMapping("/comments/{commentId}/likes")
    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 누릅니다.")
    public ResponseEntity<LikeResponseDTO> addLikeToComment(
            @PathVariable Long commentId
    ){
        LikeResponseDTO result = likeService.addLikeToComment(commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @BoardAccess(operation = OperationType.WRITE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GUEST', 'STUDENT')")
    @DeleteMapping("/posts/{postId}/likes")
    @Operation(summary = "게시글 좋아요 제거", description = "좋아요를 눌렀던 게시글에 좋아요를 제거합니다.")
    public ResponseEntity<Void> removeLikeToPost(
            @PathVariable Long postId
    ){
        likeService.removeLikeFromPost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @BoardAccess(operation = OperationType.WRITE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GUEST', 'STUDENT')")
    @DeleteMapping("/comments/{commentId}/likes")
    @Operation(summary = "댓글 좋아요 제거", description = "좋아요를 눌렀던 댓글에 좋아요를 제거합니다.")
    public ResponseEntity<Void> removeLikeToComment(
            @PathVariable Long commentId
    ){
        likeService.removeLikeFromComment(commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
