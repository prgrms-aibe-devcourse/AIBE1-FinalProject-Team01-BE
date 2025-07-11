package kr.co.amateurs.server.controller.together;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.service.together.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
@Tag(name="Gathering Post", description = "함께해요 게시판의 팀원 모집 탭 API")
public class GatheringController {

    private final GatheringService gatheringService;

    @GetMapping
    @Operation(summary = "팀원 모집 글 리스트", description = "팀원 모집 탭의 모든 게시글을 검색어, 정렬기준에 따라 불러옵니다.")
    public ResponseEntity<PageResponseDTO<GatheringPostResponseDTO>> getGatheringPostList(
            @ParameterObject @Valid PostPaginationParam paginationParam
            ){
        PageResponseDTO<GatheringPostResponseDTO> gatheringList = gatheringService.getGatheringPostList(paginationParam);
        return ResponseEntity.ok(gatheringList);
    }

    @GetMapping("/{gatheringId}")
    @Operation(summary = "팀원 모집 글 정보", description = "팀원 모집 탭의 특정 게시글의 정보를 불러옵니다.")
    public ResponseEntity<GatheringPostResponseDTO> getGatheringPost(
            @PathVariable("gatheringId") Long gatheringId,
            HttpServletRequest request){
        String ipAddress = request.getRemoteAddr();
        GatheringPostResponseDTO gatherPost = gatheringService.getGatheringPost(gatheringId, ipAddress);
        return ResponseEntity.ok(gatherPost);
    }

    @PostMapping
    @Operation(summary = "팀원 모집 글쓰기", description = "팀원 모집 탭에 새로운 게시글을 등록합니다.")
    public ResponseEntity<GatheringPostResponseDTO> createGatheringPost(
            @RequestBody @Valid GatheringPostRequestDTO dto){
        GatheringPostResponseDTO post = gatheringService.createGatheringPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{gatheringId}")
    @Operation(summary = "팀원 모집 글 수정", description = "팀원 모집 탭의 본인이 작성한 게시글을 수정합니다.")
    public ResponseEntity<Void> updateGatheringPost(

            @PathVariable("gatheringId") Long gatheringId,
            @RequestBody @Valid GatheringPostRequestDTO dto){
        gatheringService.updateGatheringPost(gatheringId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @DeleteMapping("/{gatheringId}")
    @Operation(summary = "팀원 모집 글 삭제", description = "팀원 모집 탭의 본인이 작성한 게시글을 삭제합니다.")
    public ResponseEntity<Void> deleteGatheringPost(
            @PathVariable("gatheringId") Long gatheringId){
        gatheringService.deleteGatheringPost(gatheringId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
