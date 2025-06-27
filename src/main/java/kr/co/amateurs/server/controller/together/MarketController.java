package kr.co.amateurs.server.controller.together;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.Operation;
import kr.co.amateurs.server.service.together.MarketService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
@Tag(name="Gathering Post", description = "함께해요 게시판의 장터 탭 API")
public class MarketController {
    private final MarketService marketService;

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasBoardType = false, boardType = BoardType.MARKET)
    @GetMapping
    public ResponseEntity<PageResponseDTO<MarketPostResponseDTO>> getMarketPostList(
            @ParameterObject @Valid PostPaginationParam paginationParam
            ){
        PageResponseDTO<MarketPostResponseDTO> marketList = marketService.getMarketPostList(paginationParam);
        return ResponseEntity.ok(marketList);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasPostId = true)
    @GetMapping("/{postId}")
    public ResponseEntity<MarketPostResponseDTO> getMarketPost(@PathVariable("postId") Long postId){
        MarketPostResponseDTO gatherPost = marketService.getMarketPost(postId);
        return ResponseEntity.ok(gatherPost);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasBoardType = false, boardType = BoardType.MARKET, operation = Operation.WRITE)
    @PostMapping
    public ResponseEntity<MarketPostResponseDTO> createMarketPost(
            @RequestBody @Valid MarketPostRequestDTO dto){
        MarketPostResponseDTO post = marketService.createMarketPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateMarketPost(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid MarketPostRequestDTO dto){
        marketService.updateMarketPost(postId, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO - Soft Delete 로 변경 시 PATCH 요청으로 변경 예정
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = Operation.WRITE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteMarketPost(
            @PathVariable("postId") Long postId){
        marketService.deleteMarketPost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
