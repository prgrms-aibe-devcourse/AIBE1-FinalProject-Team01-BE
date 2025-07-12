package kr.co.amateurs.server.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.auth.*;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request,
                                                  HttpServletResponse response) {
        LoginResponseDTO loginResponse = authService.login(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/check/email")
    @Operation(summary = "이메일 중복 확인", description = "이메일 중복 여부를 확인합니다")
    public ResponseEntity<CheckResponseDTO> checkEmailDuplicate(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(available ?
                CheckResponseDTO.available("이메일") :
                CheckResponseDTO.unavailable("이메일"));
    }

    @GetMapping("/check/nickname")
    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 여부를 확인합니다")
    public ResponseEntity<CheckResponseDTO> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(available ?
                CheckResponseDTO.available("닉네임") :
                CheckResponseDTO.unavailable("닉네임"));
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 이용하여 새로운 액세스 토큰을 발급합니다")
    public ResponseEntity<TokenReissueResponseDTO> reissueToken(@Valid @RequestBody TokenReissueRequestDTO request) {
        TokenReissueResponseDTO response = authService.reissueToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 Refresh Token을 삭제합니다")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LogoutResponseDTO> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(LogoutResponseDTO.success());
    }

    @PostMapping("/complete-profile")
    @Operation(summary = "소셜 프로필 완성", description = "소셜 로그인 사용자의 프로필을 완성합니다")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileCompleteResponseDTO> completeProfile(@Valid @RequestBody ProfileCompleteRequestDTO request) {
        ProfileCompleteResponseDTO response = authService.completeProfile(request);
        return ResponseEntity.ok(response);
    }
}