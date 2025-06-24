package kr.co.amateurs.server.controller.community;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<Page<CommunityResponseDTO>> getCommunity(
            @PathVariable BoardType boardType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "LATEST") SortType sortType,
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int pageSize
            ) {

        Page<CommunityResponseDTO> postsPage = communityPostService.searchPosts(keyword, page, boardType, sortType, pageSize);

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

    @PutMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId,
            @RequestBody @Valid CommunityRequestDTO requestDTO
    ){
        communityPostService.updatePost(requestDTO, postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{boardType}/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId
    ){
        communityPostService.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}