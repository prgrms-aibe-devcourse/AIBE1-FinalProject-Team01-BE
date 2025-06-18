package kr.co.amateurs.server.controller.community;

import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.dto.community.CommunityPageDTO;
import kr.co.amateurs.server.service.community.CommunityPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityPostService communityPostService;

    //@PreAuthorize("hasAnyAuthority('USER', 'STUDENT','ADMIN')")
    @GetMapping("/{boardType}")
    public ResponseEntity<CommunityPageDTO> getCommunity(
            @PathVariable BoardType boardType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "LATEST") SortType sortType) {

        CommunityPageDTO posts = communityPostService.searchPosts(keyword, page, boardType, sortType);

        return ResponseEntity.ok(posts);
    }

    //@PreAuthorize("hasAnyAuthority('USER', 'STUDENT','ADMIN')")
    @GetMapping("/{boardType}/{postId}")
    public ResponseEntity<CommunityResponseDTO> getCommunityPost(
            @PathVariable BoardType boardType,
            @PathVariable Long postId) {

        CommunityResponseDTO post = communityPostService.getPost(boardType, postId);

        return ResponseEntity.ok(post);
    }
}
