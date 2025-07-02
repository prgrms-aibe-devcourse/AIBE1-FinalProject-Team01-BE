package kr.co.amateurs.server.controller.community;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.service.community.CommunityService;
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
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping("/{boardType}")
    @Operation(
            summary = "커뮤니티 게시글 목록 조회",
            description = "특정 게시판 타입의 게시글 목록을 페이지네이션으로 조회합니다. 키워드 검색도 지원합니다."
    )
    public ResponseEntity<PageResponseDTO<CommunityResponseDTO>> getCommunity(
            @PathVariable BoardType boardType,
            @ParameterObject @Valid PostPaginationParam paginationParam
            ) {

        PageResponseDTO<CommunityResponseDTO> postsPage = communityService.searchPosts(boardType, paginationParam);

        return ResponseEntity.ok(postsPage);
    }

    @GetMapping("/{boardType}/{communityId}")
    @Operation(
            summary = "커뮤니티 게시글 상세 조회",
            description = "특정 게시글의 상세 정보를 조회합니다."
    )
    public ResponseEntity<CommunityResponseDTO> getCommunityPost(
            @PathVariable BoardType boardType,
            @PathVariable Long communityId) {

        CommunityResponseDTO post = communityService.getPost(communityId);

        return ResponseEntity.ok(post);
    }

    @Operation(
            summary = "커뮤니티 게시글 작성",
            description = "새로운 커뮤니티 게시글을 작성합니다. 해당 게시판의 쓰기 권한이 있어야 합니다."
    )
    @PostMapping("/{boardType}")
    public ResponseEntity<CommunityResponseDTO> createPost(
            @PathVariable BoardType boardType,
            @RequestBody @Valid CommunityRequestDTO requestDTO
    ){
        CommunityResponseDTO post = communityService.createPost(requestDTO, boardType);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @Operation(
            summary = "커뮤니티 게시글 수정",
            description = "기존 게시글을 수정합니다. 게시글 작성자만 수정할 수 있습니다."
    )
    @PutMapping("/{boardType}/{communityId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable BoardType boardType,
            @PathVariable Long communityId,
            @RequestBody @Valid CommunityRequestDTO requestDTO
    ){
        communityService.updatePost(requestDTO, communityId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "커뮤니티 게시글 삭제",
            description = "게시글을 삭제합니다. 게시글 작성자만 삭제할 수 있습니다."
    )
    @DeleteMapping("/{boardType}/{communityId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable BoardType boardType,
            @PathVariable Long communityId
    ){
        communityService.deletePost(communityId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}