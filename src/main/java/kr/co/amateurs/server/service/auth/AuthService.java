package kr.co.amateurs.server.service.auth;

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
        User user = userService.findByEmail(request.email());

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw ErrorCode.INVALID_PASSWORD.get();
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        Long expiresIn = jwtProvider.getAccessTokenExpirationMs();

        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs() / 1000;

        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken, refreshExpiresIn);

        return LoginResponseDto.of(accessToken, refreshToken, expiresIn);
    }

    @Transactional
    public TokenReissueResponseDTO reissueToken (TokenReissueRequestDTO request){
        String refreshToken = request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw ErrorCode.UNAUTHORIZED.get();
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);

        if (!refreshTokenService.existsByEmail(email)) {
            throw ErrorCode.UNAUTHORIZED.get();
        }

        String newAccessToken = jwtProvider.generateAccessToken(email);
        Long expiresIn = jwtProvider.getAccessTokenExpirationMs();

        return TokenReissueResponseDTO.of(newAccessToken, expiresIn);
    }
}
