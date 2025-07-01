package kr.co.amateurs.server.controller.it;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.it.ITService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/IT")
@RequiredArgsConstructor
@Tag(name="IT", description = "IT 게시판 관련 API")
public class ITController {
    private final ITService itService;

    @Operation(
            summary = "IT 게시글 목록 조회",
            description = "IT 게시판의 게시글 목록을 페이지네이션으로 조회합니다. 키워드 검색을 지원합니다."
    )
    @GetMapping("/{boardType}")
    public ResponseEntity<PageResponseDTO<ITResponseDTO>> searchPosts(
            @PathVariable BoardType boardType,
            @ParameterObject @Valid PostPaginationParam paginationParam
    ) {
        PageResponseDTO<ITResponseDTO> postsPage = itService.searchPosts(boardType, paginationParam);

        return ResponseEntity.ok(postsPage);
    }

    @Operation(
            summary = "IT 게시글 상세 조회",
            description = "특정 IT 게시글의 상세 정보를 조회합니다."
    )
    @GetMapping("/{boardType}/{itId}")
    public ResponseEntity<ITResponseDTO> getPost(
            @PathVariable BoardType boardType,
            @PathVariable Long itId) {
        ITResponseDTO post = itService.getPost(itId);

        return ResponseEntity.ok(post);
    }


    @Operation(
            summary = "IT 게시글 작성",
            description = "새로운 IT 게시글을 작성합니다. 해당 게시판의 쓰기 권한이 있어야 합니다."
    )
    @PostMapping("/{boardType}")
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
    @PutMapping("/{boardType}/{itId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable BoardType boardType,
            @PathVariable Long itId,
            @RequestBody ITRequestDTO requestDTO
    ){
        itService.updatePost(requestDTO, itId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "IT 게시글 삭제",
            description = "IT 게시글을 삭제합니다. 게시글 작성자만 삭제할 수 있습니다."
    )
    @DeleteMapping("/{boardType}/{itId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable BoardType boardType,
            @PathVariable Long itId
    ){
        itService.deletePost(itId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
