package kr.co.amateurs.server.controller.it;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.it.ITService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/IT")
@RequiredArgsConstructor
public class ITController {
    private final ITService itService;

    @Operation(
            summary = "IT 게시글 목록 조회",
            description = "IT 게시판의 게시글 목록을 페이지네이션으로 조회합니다. 키워드 검색을 지원합니다."
    )
    @BoardAccess(needCategory = true, category = BoardCategory.IT)
    @GetMapping("/{boardType}")
    public ResponseEntity<Page<ITResponseDTO>> searchPosts(
            @PathVariable BoardType boardType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "LATEST") SortType sortType,
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int pageSize
    ) {
        Page<ITResponseDTO> postsPage = itService.searchPosts(keyword, page,boardType, sortType, pageSize);

        return ResponseEntity.ok(postsPage);
    }

    @Operation(
            summary = "IT 게시글 상세 조회",
            description = "특정 IT 게시글의 상세 정보를 조회합니다."
    )
    @BoardAccess(needCategory = true, category = BoardCategory.IT, hasPostId = true)
    @GetMapping("/{boardType}/{postId}")
    public ResponseEntity<ITResponseDTO> getPost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId) {
        ITResponseDTO post = itService.getPost(postId);

        return ResponseEntity.ok(post);
    }

    @BoardAccess(needCategory = true, category = BoardCategory.IT, operation = OperationType.WRITE)
    @PostMapping("/{boardType}")
    @Operation(
            summary = "IT 게시글 작성",
            description = "새로운 IT 게시글을 작성합니다. 해당 게시판의 쓰기 권한이 있어야 합니다."
    )
    public ResponseEntity<ITResponseDTO> createPost(
            @PathVariable BoardType boardType,
            @Valid @RequestBody ITRequestDTO requestDTO
    ){
        ITResponseDTO post = itService.createPost(requestDTO, boardType);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @Operation(
            summary = "IT 게시글 수정",
            description = "기존 IT 게시글을 수정합니다. 게시글 작성자만 수정할 수 있습니다."
    )
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE)
    @PutMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId,
            @RequestBody ITRequestDTO requestDTO
    ){
        itService.updatePost(requestDTO, postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "IT 게시글 삭제",
            description = "IT 게시글을 삭제합니다. 게시글 작성자만 삭제할 수 있습니다."
    )
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE)
    @DeleteMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId
    ){
        itService.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
