package kr.co.amateurs.server.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.user.*;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인 한 사용자의 프로필 정보를 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser() {
        UserProfileResponseDTO response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기본 정보 수정", description = "현재 로그인 한 사용자의 기본 프로필을 수정합니다")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserBasicProfileEditResponseDTO> updateBasicProfile(
            @Valid @RequestBody UserBasicProfileEditRequestDTO request) {
        UserBasicProfileEditResponseDTO response = userService.updateBasicProfile(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새로운 비밀번호로 변경합니다")
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPasswordEditResponseDTO> updatePassword(
            @Valid @RequestBody UserPasswordEditRequestDTO request) {
        UserPasswordEditResponseDTO response = userService.updatePassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "관심 주제 변경", description = "사용자의 관심 주제를 변경합니다")
    @PutMapping("/me/topics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserTopicsEditResponseDTO> updateTopics(
            @Valid @RequestBody UserTopicsEditRequestDTO request) {
        UserTopicsEditResponseDTO response = userService.updateTopics(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 탈퇴합니다")
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDeleteResponseDTO> deleteUser(
            @RequestBody UserDeleteRequestDTO request) {
        UserDeleteResponseDTO response = userService.deleteUser(request);
        return ResponseEntity.ok(response);
    }
}
