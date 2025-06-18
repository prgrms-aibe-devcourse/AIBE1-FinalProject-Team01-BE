package kr.co.amateurs.server.controller.together;


import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.service.together.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @GetMapping
    public ResponseEntity<List<GatheringPostResponseDTO>> getGatheringPostList(){
        List<GatheringPostResponseDTO> gatheringList = gatheringService.getGatheringPostList();
        return ResponseEntity.ok(gatheringList);
    }

    @GetMapping("/{gatheringId}")
    public ResponseEntity<GatheringPostResponseDTO> getGatheringPost(@PathVariable("gatheringId") Long gatheringId){
        GatheringPostResponseDTO gatherPost = gatheringService.getGatheringPost(gatheringId);
        return ResponseEntity.ok(gatherPost);
    }

    @PostMapping
    public ResponseEntity<GatheringPostResponseDTO> createGatheringPost(@RequestBody GatheringPostRequestDTO dto){
        GatheringPostResponseDTO post = gatheringService.createGatheringPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{gatheringId}")
    public ResponseEntity<Void> updateGatheringPost(@PathVariable("gatheringId") Long gatheringId, @RequestBody GatheringPostRequestDTO dto){
        gatheringService.updateGatheringPost(gatheringId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @DeleteMapping("/{gatheringId}")
    public ResponseEntity<Void> deleteGatheringPost(@PathVariable("gatheringId") Long gatheringId){
        gatheringService.deleteGatheringPost(gatheringId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
