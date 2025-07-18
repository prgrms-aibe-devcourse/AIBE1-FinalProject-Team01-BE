package kr.co.amateurs.server.controller.together;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.service.together.MatchService;
import kr.co.amateurs.server.utils.ClientIPUtils;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Tag(name="Matching", description = "함께해요 게시판의 커피챗/멘토링 탭 API")
public class MatchController {
    
    private final MatchService matchService;

    @GetMapping
    @Operation(summary = "커피챗/멘토링 글 리스트", description = "커피챗/멘토링 탭의 모든 게시글을 검색어, 정렬기준에 따라 불러옵니다.")
    public ResponseEntity<PageResponseDTO<MatchPostResponseDTO>> getMatchPostList(
            @ParameterObject @Valid PostPaginationParam paginationParam
            ){
        PageResponseDTO<MatchPostResponseDTO> matchList = matchService.getMatchPostList(paginationParam);
        return ResponseEntity.ok(matchList);
    }

    @GetMapping("/{matchId}")
    @Operation(summary = "커피챗/멘토링 글 정보", description = "커피챗/멘토링 탭의 특정 게시글의 정보를 불러옵니다.")
    public ResponseEntity<MatchPostResponseDTO> getMatchPost(
            @PathVariable("matchId") Long matchId,
            HttpServletRequest request){
        String ipAddress = ClientIPUtils.getClientIP(request);
        MatchPostResponseDTO gatherPost = matchService.getMatchPost(matchId, ipAddress);
        return ResponseEntity.ok(gatherPost);
    }

    @PostMapping
    @Operation(summary = "커피챗/멘토링 글쓰기", description = "커피챗/멘토링 탭에 새로운 게시글을 등록합니다.")
    public ResponseEntity<MatchPostResponseDTO> createMatchPost(
            @RequestBody @Valid MatchPostRequestDTO dto){
        MatchPostResponseDTO post = matchService.createMatchPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{matchId}")
    @Operation(summary = "커피챗/멘토링 글 수정", description = "커피챗/멘토링 탭의 본인이 작성한 게시글을 수정합니다.")
    public ResponseEntity<Void> updateMatchPost(
            @PathVariable("matchId") Long matchId,
            @RequestBody @Valid MatchPostRequestDTO dto){
        matchService.updateMatchPost(matchId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{matchId}/{status}")
    @Operation(summary = "팀원 모집 글 상태 수정", description = "팀원 모집 탭의 본인이 작성한 게시글의 상태를 수정합니다.")
    public ResponseEntity<Void> updateMarketPostStatus(
            @PathVariable("matchId") Long matchId,
            @PathVariable("status") MatchingStatus status
    ){
        matchService.updateMatchPostStatus(matchId, status);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @DeleteMapping("/{matchId}")
    @Operation(summary = "커피챗/멘토링 글 삭제", description = "커피챗/멘토링 탭의 본인이 작성한 게시글을 삭제합니다.")
    public ResponseEntity<Void> deleteMatchPost(
            @PathVariable("matchId") Long matchId){
        matchService.deleteMatchPost(matchId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
