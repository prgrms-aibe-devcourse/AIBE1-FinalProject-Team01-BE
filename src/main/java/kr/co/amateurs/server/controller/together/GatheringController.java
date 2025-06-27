package kr.co.amateurs.server.controller.together;


import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.service.together.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasBoardType = false, boardType = BoardType.GATHER)
    @GetMapping
    public ResponseEntity<PageResponseDTO<GatheringPostResponseDTO>> getGatheringPostList(
            @ParameterObject @Valid PostPaginationParam paginationParam
            ){
        PageResponseDTO<GatheringPostResponseDTO> gatheringList = gatheringService.getGatheringPostList(paginationParam);
        return ResponseEntity.ok(gatheringList);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<GatheringPostResponseDTO> getGatheringPost(
            @PathVariable("postId") Long postId){
        GatheringPostResponseDTO gatherPost = gatheringService.getGatheringPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasBoardType = false, boardType = BoardType.GATHER, operation = OperationType.WRITE)
    @PostMapping
    public ResponseEntity<GatheringPostResponseDTO> createGatheringPost(
            @RequestBody @Valid GatheringPostRequestDTO dto){
        GatheringPostResponseDTO post = gatheringService.createGatheringPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE)
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateGatheringPost(

            @PathVariable("postId") Long postId,
            @RequestBody GatheringPostRequestDTO dto){
        gatheringService.updateGatheringPost(postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteGatheringPost(
            @PathVariable("postId") Long postId){
        gatheringService.deleteGatheringPost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
