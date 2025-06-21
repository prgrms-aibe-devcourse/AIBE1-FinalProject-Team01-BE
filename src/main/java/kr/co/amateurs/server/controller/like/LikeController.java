package kr.co.amateurs.server.controller.like;


import kr.co.amateurs.server.domain.dto.like.LikeRequestDTO;
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
            @PathVariable Long postId,
            @RequestBody LikeRequestDTO likeRequestDTO
    ){
        LikeResponseDTO result = likeService.addLikeToPost(postId, likeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponseDTO> addLikeToComment(
            @PathVariable Long commentId,
            @RequestBody LikeRequestDTO likeRequestDTO
    ){
        LikeResponseDTO result = likeService.addLikeToComment(commentId, likeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
