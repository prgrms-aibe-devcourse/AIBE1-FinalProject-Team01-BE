package kr.co.amateurs.server.controller.follow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import kr.co.amateurs.server.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "유저 간 팔로우 API")
public class FollowController {
    private final FollowService followService;

    @GetMapping("/users/following")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 유저가 팔로우 하는 유저 목록을 조회합니다.")
    public ResponseEntity<PageResponseDTO<FollowResponseDTO>> getFollowingList(
            @ParameterObject @Valid PaginationParam paginationParam
    ) {
        PageResponseDTO<FollowResponseDTO> followingList = followService.getFollowingList(paginationParam);
        return ResponseEntity.ok(followingList);
    }

    @PostMapping("/users/{targetUserId}/follow")
    @Operation(summary = "팔로우 추가", description = "특정 유저를 팔로우합니다.")
    public ResponseEntity<Void> addFollow(@PathVariable Long targetUserId) {
        followService.followUser(targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/users/{targetUserId}/follow")
    @Operation(summary = "팔로우 제거", description = "특정 유저에 대한 팔로우를 제거합니다.")
    public ResponseEntity<Void> removeFollow(@PathVariable Long targetUserId) {
        followService.unfollowUser(targetUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
