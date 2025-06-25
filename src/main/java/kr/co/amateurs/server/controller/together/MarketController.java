package kr.co.amateurs.server.controller.together;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;

    @BoardAccess(hasBoardType = false, boardType = BoardType.MARKET)
    @GetMapping
    public ResponseEntity<PageResponseDTO<MarketPostResponseDTO>> getMarketPostList(
            @RequestParam(required = false) String keyword,
            @ModelAttribute @Valid PaginationParam paginationParam
            ){
        PageResponseDTO<MarketPostResponseDTO> marketList = marketService.getMarketPostList(keyword, paginationParam);
        return ResponseEntity.ok(marketList);
    }

    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<MarketPostResponseDTO> getMarketPost(@PathVariable("postId") @NotNull Long postId){
        MarketPostResponseDTO gatherPost = marketService.getMarketPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @BoardAccess(hasBoardType = false, boardType = BoardType.MARKET, operation = Operation.WRITE)
    @PostMapping
    public ResponseEntity<MarketPostResponseDTO> createMarketPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody @Valid MarketPostRequestDTO dto){
        MarketPostResponseDTO post = marketService.createMarketPost(currentUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateMarketPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("postId") @NotNull Long postId,
            @RequestBody @Valid MarketPostRequestDTO dto){
        marketService.updateMarketPost(currentUser, postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteMarketPost(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("postId") @NotNull Long postId){
        marketService.deleteMarketPost(currentUser, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
