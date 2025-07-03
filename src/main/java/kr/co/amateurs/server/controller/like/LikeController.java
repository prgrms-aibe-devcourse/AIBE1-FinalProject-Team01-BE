package kr.co.amateurs.server.controller.like;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.annotation.checkpostmetadata.CheckPostMetaData;
import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="Like", description = "좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {
    private final LikeService likeService;

    @CheckPostMetaData
    @PostMapping("/posts/{postId}/likes")
    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 누릅니다.")
    public ResponseEntity<LikeResponseDTO> addLikeToPost(
            @PathVariable Long postId
    ){
        LikeResponseDTO result = likeService.addLikeToPost(postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @CheckPostMetaData
    @PostMapping("/posts/{postId}/comments/{commentId}/likes")
    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 누릅니다.")
    public ResponseEntity<LikeResponseDTO> addLikeToComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ){
        LikeResponseDTO result = likeService.addLikeToComment(postId, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @CheckPostMetaData
    @DeleteMapping("/posts/{postId}/likes")
    @Operation(summary = "게시글 좋아요 제거", description = "좋아요를 눌렀던 게시글에 좋아요를 제거합니다.")
    public ResponseEntity<Void> removeLikeToPost(
            @PathVariable Long postId
    ){
        likeService.removeLikeFromPost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @CheckPostMetaData
    @DeleteMapping("/posts/{postId}/comments/{commentId}/likes")
    @Operation(summary = "댓글 좋아요 제거", description = "좋아요를 눌렀던 댓글에 좋아요를 제거합니다.")
    public ResponseEntity<Void> removeLikeToComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ){
        likeService.removeLikeFromComment(postId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
