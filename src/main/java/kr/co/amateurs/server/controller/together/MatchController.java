package kr.co.amateurs.server.controller.together;

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
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {
    
    private final MatchService matchService;

    @BoardAccess(hasBoardType = false, boardType = BoardType.MATCH)
    @GetMapping
    public ResponseEntity<Page<MatchPostResponseDTO>> getMatchPostList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") SortType sortType
    ){
        Page<MatchPostResponseDTO> matchList = matchService.getMatchPostList(keyword, page, size, sortType);
        return ResponseEntity.ok(matchList);
    }

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<MatchPostResponseDTO> getMatchPost(@PathVariable("postId") Long postId){
        MatchPostResponseDTO gatherPost = matchService.getMatchPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @BoardAccess(hasBoardType = false, boardType = BoardType.MATCH, operation = Operation.WRITE)
    @PostMapping
    public ResponseEntity<MatchPostResponseDTO> createMatchPost(@RequestBody MatchPostRequestDTO dto){
        MatchPostResponseDTO post = matchService.createMatchPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateMatchPost(@PathVariable("postId") Long postId, @RequestBody MatchPostRequestDTO dto){
        matchService.updateMatchPost(postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteMatchPost(@PathVariable("postId") Long postId){
        matchService.deleteMatchPost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
