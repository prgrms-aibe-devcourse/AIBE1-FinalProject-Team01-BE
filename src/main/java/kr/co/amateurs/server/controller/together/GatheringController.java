package kr.co.amateurs.server.controller.together;


import kr.co.amateurs.server.config.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.together.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @BoardAccess(hasBoardType = false, boardType = BoardType.GATHER)
    @GetMapping
    public ResponseEntity<Page<GatheringPostResponseDTO>> getGatheringPostList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") SortType sortType
    ){
        Page<GatheringPostResponseDTO> gatheringList = gatheringService.getGatheringPostList(keyword, page, size, sortType);
        return ResponseEntity.ok(gatheringList);
    }

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<GatheringPostResponseDTO> getGatheringPost(@PathVariable("postId") Long postId){
        GatheringPostResponseDTO gatherPost = gatheringService.getGatheringPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @BoardAccess(hasBoardType = false, boardType = BoardType.GATHER, operation = "write")
    @PostMapping
    public ResponseEntity<GatheringPostResponseDTO> createGatheringPost(@RequestBody GatheringPostRequestDTO dto){
        GatheringPostResponseDTO post = gatheringService.createGatheringPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateGatheringPost(@PathVariable("postId") Long postId, @RequestBody GatheringPostRequestDTO dto){
        gatheringService.updateGatheringPost(postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteGatheringPost(@PathVariable("postId") Long postId){
        gatheringService.deleteGatheringPost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
