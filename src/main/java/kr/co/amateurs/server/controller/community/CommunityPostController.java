package kr.co.amateurs.server.controller.community;

import jakarta.validation.Valid;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityPostController {
    private final CommunityPostService communityPostService;

    @BoardAccess(needCategory = true, category = BoardCategory.COMMUNITY)
    @GetMapping("/{boardType}")
    public ResponseEntity<PageResponseDTO<CommunityResponseDTO>> getCommunity(
            @PathVariable BoardType boardType,
            @ParameterObject @Valid PostPaginationParam paginationParam
            ) {

        PageResponseDTO<CommunityResponseDTO> postsPage = communityPostService.searchPosts(boardType, paginationParam);

        return ResponseEntity.ok(postsPage);
    }

    @BoardAccess(needCategory = true, category = BoardCategory.COMMUNITY, hasPostId = true)
    @GetMapping("/{boardType}/{postId}")
    public ResponseEntity<CommunityResponseDTO> getCommunityPost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId) {

        CommunityResponseDTO post = communityPostService.getPost(postId);

        return ResponseEntity.ok(post);
    }

    @BoardAccess(needCategory = true,category = BoardCategory.COMMUNITY, operation = Operation.WRITE)
    @PostMapping("/{boardType}")
    public ResponseEntity<CommunityResponseDTO> createPost(
            @PathVariable BoardType boardType,
            @RequestBody @Valid CommunityRequestDTO requestDTO
    ){
        CommunityResponseDTO post = communityPostService.createPost(requestDTO, boardType);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @PutMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId,
            @RequestBody @Valid CommunityRequestDTO requestDTO
    ){
        communityPostService.updatePost(requestDTO, postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @DeleteMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId
    ){
        communityPostService.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}