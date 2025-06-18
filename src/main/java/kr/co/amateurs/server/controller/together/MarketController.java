package kr.co.amateurs.server.controller.together;


import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
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
    
    @GetMapping
    public ResponseEntity<Page<MarketPostResponseDTO>> getMarketPostList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") String sortType  //커뮤니티와 병합 시 SortType enum으로 수정 예정
    ){
        Page<MarketPostResponseDTO> marketList = marketService.getMarketPostList(keyword, page, size, sortType);
        return ResponseEntity.ok(marketList);
    }

    @GetMapping("/{marketId}")
    public ResponseEntity<MarketPostResponseDTO> getMarketPost(@PathVariable("marketId") Long marketId){
        MarketPostResponseDTO gatherPost = marketService.getMarketPost(marketId);
        return ResponseEntity.ok(gatherPost);
    }

    @PostMapping
    public ResponseEntity<MarketPostResponseDTO> createMarketPost(@RequestBody MarketPostRequestDTO dto){
        MarketPostResponseDTO post = marketService.createMarketPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{marketId}")
    public ResponseEntity<Void> updateMarketPost(@PathVariable("marketId") Long marketId, @RequestBody MarketPostRequestDTO dto){
        marketService.updateMarketPost(marketId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @DeleteMapping("/{marketId}")
    public ResponseEntity<Void> deleteMarketPost(@PathVariable("marketId") Long marketId){
        marketService.deleteMarketPost(marketId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
