package kr.co.amateurs.server.controller.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.annotation.checkpostmetadata.CheckPostMetaData;
import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @CheckPostMetaData
    @GetMapping("/{postId}/comments")
    @Operation(
            summary = "댓글 목록 조회",
            description = "부모 댓글만 커서 페이지네이션으로 조회됩니다."
    )
    public ResponseEntity<CommentPageDTO> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        CommentPageDTO comments = commentService.getCommentsByPostId(postId, cursor, size);
        return ResponseEntity.ok(comments);
    }

    @CheckPostMetaData
    @GetMapping("/{postId}/comments/{commentId}/replies")
    @Operation(
            summary = "대댓글 목록 조회",
            description = "특정 댓글의 대댓글 목록을 커서 페이지네이션으로 조회합니다."
    )
    public ResponseEntity<CommentPageDTO> getReplies(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long cursor,  // 대댓글 커서
            @RequestParam(defaultValue = "5") int size
    ) {
        CommentPageDTO replies = commentService.getReplies(postId, commentId, cursor, size);
        return ResponseEntity.ok(replies);
    }

    @CheckPostMetaData(operation = OperationType.WRITE)
    @PostMapping("/{postId}/comments")
    @Operation(
            summary = "댓글 작성",
            description = "새로운 댓글을 작성합니다. parentCommentId가 있으면 대댓글, 없으면 일반 댓글로 작성됩니다."
    )
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDTO requestDTO
            ){
        CommentResponseDTO comment = commentService.createComment(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @CheckPostMetaData(operation = OperationType.WRITE)
    @PutMapping("/{postId}/comments/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "댓글 작성자만 기존 댓글의 내용을 수정합니다."
    )
    public ResponseEntity<Void> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDTO requestDTO
    ){
        commentService.updateComment(postId, commentId, requestDTO);
        return ResponseEntity.noContent().build();
    }

    @CheckPostMetaData(operation = OperationType.WRITE)
    @DeleteMapping("/{postId}/comments/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "댓글 작성자만 댓글을 삭제합니다."
    )
    public ResponseEntity<Void> deleteComment(
        @PathVariable Long postId,
        @PathVariable Long commentId
    ){
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
