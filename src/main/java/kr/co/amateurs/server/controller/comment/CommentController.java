package kr.co.amateurs.server.controller.comment;

import jakarta.validation.Valid;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
import kr.co.amateurs.server.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}/comments")
    public ResponseEntity<CommentPageDTO> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        CommentPageDTO comments = commentService.getCommentsByPostId(postId, cursor, size);
        return ResponseEntity.ok(comments);
    }

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}/comments/{commentId}/replies")
    public ResponseEntity<CommentPageDTO> getReplies(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int size
    ) {
        CommentPageDTO replies = commentService.getReplies(commentId, cursor, size);
        return ResponseEntity.ok(replies);
    }

    @BoardAccess(hasPostId = true, operation = Operation.WRITE)
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDTO requestDTO
            ){
        CommentResponseDTO comment = commentService.createComment(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @BoardAccess(isComment = true, checkAuthor = true, hasPostId = true, operation = Operation.WRITE)
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDTO requestDTO
    ){
        commentService.updateComment(commentId, requestDTO);
        return ResponseEntity.noContent().build();
    }

    @BoardAccess(isComment = true, checkAuthor = true, hasPostId = true, operation = Operation.WRITE)
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable Long postId,
        @PathVariable Long commentId
    ){
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
