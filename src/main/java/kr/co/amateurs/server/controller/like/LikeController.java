package kr.co.amateurs.server.controller.like;


import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<LikeResponseDTO> addLikeToPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull Long postId
    ){
        Long userId = currentUser.getUser().getId();
        LikeResponseDTO result = likeService.addLikeToPost(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponseDTO> addLikeToComment(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull Long commentId
    ){
        Long userId = currentUser.getUser().getId();
        LikeResponseDTO result = likeService.addLikeToComment(commentId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> removeLikeToPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull Long postId
    ){
        Long userId = currentUser.getUser().getId();
        likeService.removeLikeFromPost(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping("/comments/{commentId}/likes")
    public ResponseEntity<Void> removeLikeToComment(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull Long commentId
    ){
        Long userId = currentUser.getUser().getId();
        likeService.removeLikeFromComment(commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
