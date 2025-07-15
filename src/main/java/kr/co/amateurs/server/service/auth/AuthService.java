package kr.co.amateurs.server.service.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.amateurs.server.config.auth.CookieUtils;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.*;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.ai.AiProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final AiProfileService aiProfileService;
    private final CookieUtils cookieUtils;

    @Transactional
    public SignupResponseDTO signup(SignupRequestDTO request){
        userService.validateEmailDuplicate(request.email());
        userService.validateNicknameDuplicate(request.nickname());

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .name(request.name())
                .password(encodedPassword)
                .role(Role.GUEST)
                .providerType(ProviderType.LOCAL)
                .isProfileCompleted(true)
                .build();

        user.addUserTopics(request.topics());

        User savedUser = userService.saveUser(user);

        generateAiProfileAsync(savedUser.getId(), "회원가입");

        return SignupResponseDTO.fromEntity(savedUser, request.topics());
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request){
        return login(request, null);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request,
                                  HttpServletResponse response) {
        User user = userService.findByEmail(request.email());

        if (user.isDeleted()) {
            throw ErrorCode.USER_NOT_FOUND.get();
        }

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw ErrorCode.INVALID_PASSWORD.get();
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        Long expiresIn = jwtProvider.getAccessTokenExpirationMs();

        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs();

        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken, refreshExpiresIn);

        if (response != null) {
            TokenInfoDTO tokenInfoDTO = TokenInfoDTO.of(accessToken, expiresIn, refreshToken, refreshExpiresIn);
            cookieUtils.setAuthTokenCookie(response, tokenInfoDTO);
        }
        return LoginResponseDTO.of(accessToken, refreshToken, expiresIn);
    }

    @Transactional
    public TokenReissueResponseDTO reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw ErrorCode.UNAUTHORIZED.get();
        }

        if (!jwtProvider.validateToken(refreshToken)) {
            throw ErrorCode.UNAUTHORIZED.get();
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);

        if (!refreshTokenService.validateRefreshToken(email, refreshToken)) {
            throw ErrorCode.UNAUTHORIZED.get();
        }

        String newAccessToken = jwtProvider.generateAccessToken(email);
        Long expiresIn = jwtProvider.getAccessTokenExpirationMs();

        if (response != null) {
            TokenInfoDTO tokenInfoDTO = TokenInfoDTO.of(newAccessToken, expiresIn, refreshToken, jwtProvider.getRefreshTokenExpirationMs());
            cookieUtils.setAuthTokenCookie(response, tokenInfoDTO);
        }

        return TokenReissueResponseDTO.of(newAccessToken, expiresIn);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (CookieUtils.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Transactional
    public void logout(HttpServletResponse response) {
        User currentUser = userService.getCurrentLoginUser();
        refreshTokenService.deleteByEmail(currentUser.getEmail());

        if (response != null) {
            cookieUtils.clearAuthTokenCookie(response);
        }
    }

    @Transactional
    public ProfileCompleteResponseDTO completeProfile(ProfileCompleteRequestDTO request) {
        User currentUser = userService.getCurrentLoginUser();

        User managedUser = userService.findById(currentUser.getId());

        if (!managedUser.getNickname().equals(request.nickname())) {
            userService.validateNicknameDuplicate(request.nickname());
        }

        managedUser.completeProfile(request.name(), request.nickname(), request.topics());

        User savedUser = userService.saveUser(managedUser);

        generateAiProfileAsync(savedUser.getId(), "소셜 프로필 완성");

        return ProfileCompleteResponseDTO.fromEntity(savedUser);
    }

    private void generateAiProfileAsync(Long userId, String context) {
        CompletableFuture.runAsync(() -> {
            try {
                aiProfileService.generateInitialProfile(userId);
                log.info("{} 시 AI 프로필 생성 완료: userId={}", context, userId);
            } catch (Exception e) {
                log.warn("{} 시 AI 프로필 생성 실패: userId={}", context, userId, e);
            }
        });
    }
}
