package kr.co.amateurs.server.controller.it;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
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

    @BoardAccess(needCategory = true, category = BoardCategory.IT, hasPostId = true)
    @GetMapping("/{boardType}/{postId}")
    public ResponseEntity<ITResponseDTO> getPost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId) {
        ITResponseDTO post = itService.getPost(postId);

        return ResponseEntity.ok(post);
    }

    @BoardAccess(needCategory = true, category = BoardCategory.IT, operation = Operation.WRITE)
    @PostMapping("/{boardType}")
    public ResponseEntity<ITResponseDTO> createPost(
            @PathVariable BoardType boardType,
            @RequestBody ITRequestDTO requestDTO
    ){
        ITResponseDTO post = itService.createPost(requestDTO, boardType);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @PutMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId,
            @RequestBody ITRequestDTO requestDTO
    ){
        itService.updatePost(requestDTO, postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @DeleteMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId
    ){
        itService.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
