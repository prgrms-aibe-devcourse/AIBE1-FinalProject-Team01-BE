package kr.co.amateurs.server.controller.like;


import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<LikeResponseDTO> addLikeToPost(
            @PathVariable Long postId
    ){
        // 리팩토링으로 수정 예정 이슈#58
        Long userId = 1L;
        LikeResponseDTO result = likeService.addLikeToPost(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponseDTO> addLikeToComment(
            @PathVariable Long commentId
    ){
        // 리팩토링으로 수정 예정 이슈#58
        Long userId = 1L;
        LikeResponseDTO result = likeService.addLikeToComment(commentId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> removeLikeToPost(
            @PathVariable Long postId
    ){
        // 리팩토링으로 수정 예정 이슈#58
        Long userId = 1L;
        likeService.removeLikeFromPost(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping("/comments/{commentId}/likes")
    public ResponseEntity<Void> removeLikeToComment(
            @PathVariable Long commentId
    ){
        // 리팩토링으로 수정 예정 이슈#58
        Long userId = 1L;
        likeService.removeLikeFromComment(commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
