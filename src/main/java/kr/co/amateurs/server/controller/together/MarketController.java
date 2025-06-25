package kr.co.amateurs.server.controller.together;


import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.together.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;

    @BoardAccess(hasBoardType = false, boardType = BoardType.MARKET)
    @GetMapping
    public ResponseEntity<Page<MarketPostResponseDTO>> getMarketPostList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") SortType sortType
    ){
        Page<MarketPostResponseDTO> marketList = marketService.getMarketPostList(keyword, page, size, sortType);
        return ResponseEntity.ok(marketList);
    }

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<MarketPostResponseDTO> getMarketPost(@PathVariable("postId") Long postId){
        MarketPostResponseDTO gatherPost = marketService.getMarketPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @BoardAccess(hasBoardType = false, boardType = BoardType.MARKET, operation = Operation.WRITE)
    @PostMapping
    public ResponseEntity<MarketPostResponseDTO> createMarketPost(@RequestBody MarketPostRequestDTO dto){
        MarketPostResponseDTO post = marketService.createMarketPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateMarketPost(@PathVariable("postId") Long postId, @RequestBody MarketPostRequestDTO dto){
        marketService.updateMarketPost(postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteMarketPost(@PathVariable("postId") Long postId){
        marketService.deleteMarketPost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
