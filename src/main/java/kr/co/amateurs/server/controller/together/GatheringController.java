package kr.co.amateurs.server.controller.together;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.TogetherPaginationParam;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
import kr.co.amateurs.server.service.together.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @BoardAccess(hasBoardType = false, boardType = BoardType.GATHER)
    @GetMapping
    public ResponseEntity<PageResponseDTO<GatheringPostResponseDTO>> getGatheringPostList(
            @ParameterObject @Valid TogetherPaginationParam paginationParam
    ) {
        PageResponseDTO<GatheringPostResponseDTO> gatheringList = gatheringService.getGatheringPostList(paginationParam);
        return ResponseEntity.ok(gatheringList);
    }

    @BoardAccess(hasBoardType = false, boardType = BoardType.GATHER)
    @GetMapping("test")
    public ResponseEntity<Page<Post>> getPostList(
            @ParameterObject @Valid TogetherPaginationParam paginationParam
    ) {
        Page<Post> posts = gatheringService.getPosts(paginationParam);
        return ResponseEntity.ok(posts);
    }

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<GatheringPostResponseDTO> getGatheringPost(
            @PathVariable("postId") @NotNull Long postId) {
        GatheringPostResponseDTO gatherPost = gatheringService.getGatheringPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @BoardAccess(hasBoardType = false, boardType = BoardType.GATHER, operation = Operation.WRITE)
    @PostMapping
    public ResponseEntity<GatheringPostResponseDTO> createGatheringPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody @Valid GatheringPostRequestDTO dto) {
        GatheringPostResponseDTO post = gatheringService.createGatheringPost(currentUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateGatheringPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("postId") Long postId,
            @RequestBody GatheringPostRequestDTO dto) {
        gatheringService.updateGatheringPost(currentUser, postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteGatheringPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("postId") Long postId) {
        gatheringService.deleteGatheringPost(currentUser, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
