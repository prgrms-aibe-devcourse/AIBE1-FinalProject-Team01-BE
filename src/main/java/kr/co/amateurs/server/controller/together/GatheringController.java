package kr.co.amateurs.server.controller.together;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.common.PageInfo;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.together.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static kr.co.amateurs.server.domain.entity.post.enums.SortType.LATEST;


@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<GatheringPostResponseDTO>> getGatheringPostList(
            @RequestParam(required = false) String keyword,
            @ModelAttribute @Valid PaginationParam paginationParam
            ){
        PageResponseDTO<GatheringPostResponseDTO> gatheringList = gatheringService.getGatheringPostList(keyword, paginationParam);
        return ResponseEntity.ok(gatheringList);
    }

    @GetMapping("/{gatheringId}")
    public ResponseEntity<GatheringPostResponseDTO> getGatheringPost(
            @PathVariable("gatheringId") @NotNull Long gatheringId){
        GatheringPostResponseDTO gatherPost = gatheringService.getGatheringPost(gatheringId);
        return ResponseEntity.ok(gatherPost);
    }

    @PostMapping
    public ResponseEntity<GatheringPostResponseDTO> createGatheringPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody @Valid GatheringPostRequestDTO dto){
        GatheringPostResponseDTO post = gatheringService.createGatheringPost(currentUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{gatheringId}")
    public ResponseEntity<Void> updateGatheringPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("gatheringId") Long gatheringId,
            @RequestBody GatheringPostRequestDTO dto){
        gatheringService.updateGatheringPost(currentUser, gatheringId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @DeleteMapping("/{gatheringId}")
    public ResponseEntity<Void> deleteGatheringPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("gatheringId") Long gatheringId){
        gatheringService.deleteGatheringPost(currentUser, gatheringId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
