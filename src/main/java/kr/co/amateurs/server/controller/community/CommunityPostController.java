package kr.co.amateurs.server.controller.community;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community", description = "커뮤니티 게시판 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityPostController {
    private final CommunityPostService communityPostService;

    @BoardAccess(needCategory = true, category = BoardCategory.COMMUNITY)
    @GetMapping("/{boardType}")
    @Operation(
            summary = "커뮤니티 게시글 목록 조회",
            description = "특정 게시판 타입의 게시글 목록을 페이지네이션으로 조회합니다. 키워드 검색도 지원합니다."
    )
    public ResponseEntity<PageResponseDTO<CommunityResponseDTO>> getCommunity(
            @PathVariable BoardType boardType,
            @ParameterObject @Valid PostPaginationParam paginationParam
            ) {

        PageResponseDTO<CommunityResponseDTO> postsPage = communityPostService.searchPosts(boardType, paginationParam);

        return ResponseEntity.ok(postsPage);
    }

    @BoardAccess(needCategory = true, category = BoardCategory.COMMUNITY, hasPostId = true)
    @GetMapping("/{boardType}/{postId}")
    @Operation(
            summary = "커뮤니티 게시글 상세 조회",
            description = "특정 게시글의 상세 정보를 조회합니다."
    )
    public ResponseEntity<CommunityResponseDTO> getCommunityPost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId) {

        CommunityResponseDTO post = communityPostService.getPost(postId);

        return ResponseEntity.ok(post);
    }

    @Operation(
            summary = "커뮤니티 게시글 작성",
            description = "새로운 커뮤니티 게시글을 작성합니다. 해당 게시판의 쓰기 권한이 있어야 합니다."
    )
    @BoardAccess(needCategory = true,category = BoardCategory.COMMUNITY, operation = OperationType.WRITE)
    @PostMapping("/{boardType}")
    public ResponseEntity<CommunityResponseDTO> createPost(
            @PathVariable BoardType boardType,
            @RequestBody @Valid CommunityRequestDTO requestDTO
    ){
        CommunityResponseDTO post = communityPostService.createPost(requestDTO, boardType);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @Operation(
            summary = "커뮤니티 게시글 수정",
            description = "기존 게시글을 수정합니다. 게시글 작성자만 수정할 수 있습니다."
    )
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE)
    @PutMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId,
            @RequestBody @Valid CommunityRequestDTO requestDTO
    ){
        communityPostService.updatePost(requestDTO, postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "커뮤니티 게시글 삭제",
            description = "게시글을 삭제합니다. 게시글 작성자만 삭제할 수 있습니다."
    )
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE)
    @DeleteMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId
    ){
        communityPostService.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}