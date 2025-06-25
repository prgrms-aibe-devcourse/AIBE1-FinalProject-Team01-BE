package kr.co.amateurs.server.controller.together;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.together.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {
    
    private final MatchService matchService;

    @BoardAccess(hasBoardType = false, boardType = BoardType.MATCH)
    @GetMapping
    public ResponseEntity<PageResponseDTO<MatchPostResponseDTO>> getMatchPostList(
            @RequestParam(required = false) String keyword,
            @ModelAttribute @Valid PaginationParam paginationParam
            ){
        PageResponseDTO<MatchPostResponseDTO> matchList = matchService.getMatchPostList(keyword, paginationParam);
        return ResponseEntity.ok(matchList);
    }

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<MatchPostResponseDTO> getMatchPost(
            @PathVariable("postId") @NotNull Long postId){
        MatchPostResponseDTO gatherPost = matchService.getMatchPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @BoardAccess(hasBoardType = false, boardType = BoardType.MATCH, operation = Operation.WRITE)
    @PostMapping
    public ResponseEntity<MatchPostResponseDTO> createMatchPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody @Valid MatchPostRequestDTO dto){
        MatchPostResponseDTO post = matchService.createMatchPost(currentUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateMatchPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("postId") @NotNull Long postId,
            @RequestBody @Valid MatchPostRequestDTO dto){
        matchService.updateMatchPost(currentUser, postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteMatchPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("postId") @NotNull Long postId){
        matchService.deleteMatchPost(currentUser, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
