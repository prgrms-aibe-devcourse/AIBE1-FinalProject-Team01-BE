package kr.co.amateurs.server.controller.follow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.domain.dto.follow.FollowPostResponseDTO;
import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import kr.co.amateurs.server.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/users/{userId}/following")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 유저가 팔로우 하는 유저 목록을 조회합니다.")
    public ResponseEntity<List<FollowResponseDTO>> getFollowingList(@PathVariable Long userId) {
        List<FollowResponseDTO> followingList = followService.getFollowingList(userId);
        return ResponseEntity.ok(followingList);
    }

    @GetMapping("/users/{userId}/follower")
    @Operation(summary = "팔로워 목록 조회", description = "특정 유저를 팔로우 하는 유저 목록을 조회합니다.")
    public ResponseEntity<List<FollowResponseDTO>> getFollowerList(@PathVariable Long userId) {
        List<FollowResponseDTO> followerList = followService.getFollowerList(userId);
        return ResponseEntity.ok(followerList);
    }

    @GetMapping("/users/{userId}/followPost")
    @Operation(summary = "팔로우 하는 유저의 게시글 목록 조회", description = "팔로우 하고 있는 유저들의 게시글을 모아 목록으로 조회합니다.")
    public ResponseEntity<List<FollowPostResponseDTO>> getFollowPostList(@PathVariable Long userId) {
        List<FollowPostResponseDTO> followPostList = followService.getFollowPostList(userId);
        return ResponseEntity.ok(followPostList);
    }

    @PostMapping("/users/{targetUserId}/follow")
    @Operation(summary = "팔로우 추가", description = "특정 유저를 팔로우합니다.")
    public ResponseEntity<FollowResponseDTO> addFollow(@PathVariable Long targetUserId) {
        FollowResponseDTO result = followService.followUser(targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/users/{targetUserId}/follow")
    @Operation(summary = "팔로우 제거", description = "특정 유저에 대한 팔로우를 제거합니다.")
    public ResponseEntity<Void> removeFollow(@PathVariable Long targetUserId) {
        followService.unfollowUser(targetUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
