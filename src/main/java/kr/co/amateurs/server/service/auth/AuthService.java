package kr.co.amateurs.server.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.amateurs.server.config.auth.CookieUtils;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDto;
import kr.co.amateurs.server.domain.dto.auth.LoginResponseDto;
import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.domain.dto.auth.SignupResponseDto;
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
    public SignupResponseDto signup(SignupRequestDto request){
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
                .build();

        user.addUserTopics(request.topics());

        User savedUser = userService.saveUser(user);

        CompletableFuture.runAsync(() -> {
            try {
                aiProfileService.generateInitialProfile(savedUser.getId());
                log.info("회원가입 시 초기 AI 프로필 생성 완료: userId={}", savedUser.getId());
            } catch (Exception e) {
                log.warn("회원가입 시 초기 AI 프로필 생성 실패: userId={}", savedUser.getId(), e);
            }
        });

        return SignupResponseDto.fromEntity(savedUser, request.topics());
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request){
        return login(request, null);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request,
                                  HttpServletResponse response) {
        User user = userService.findByEmail(request.email());

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw ErrorCode.INVALID_PASSWORD.get();
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        Long expiresIn = jwtProvider.getAccessTokenExpirationMs();

        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs() / 1000;

        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken, refreshExpiresIn);

        if (response != null) {
            cookieUtils.setAuthTokenCookie(response, accessToken, expiresIn, refreshToken, refreshExpiresIn);
        }
        return LoginResponseDto.of(accessToken, refreshToken, expiresIn);
    }
}
