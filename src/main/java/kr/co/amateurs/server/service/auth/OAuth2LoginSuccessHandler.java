package kr.co.amateurs.server.service.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${oauth.success-redirect-url:http://localhost:3000}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        log.debug("GitHub OAuth2User 속성: {}", oAuth2User.getAttributes());

        try {
            String providerId = extractGitHubProviderId(oAuth2User);

            User user = userRepository.findByProviderIdAndProviderType(providerId, ProviderType.GITHUB)
                    .orElseThrow(ErrorCode.USER_NOT_FOUND);

            log.info("GitHub 로그인 성공: userId={}, providerId={}", user.getId(), providerId);

            String accessToken = jwtProvider.generateAccessToken(user.getEmail());
            Long accessExpiresIn = jwtProvider.getAccessTokenExpirationMs();

            String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
            Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs() / 1000;

            refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken, refreshExpiresIn);

            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setMaxAge(Math.toIntExact(accessExpiresIn / 1000));
            accessTokenCookie.setPath("/");
            accessTokenCookie.setSecure(false);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setMaxAge(Math.toIntExact(refreshExpiresIn));
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setSecure(false);
            response.addCookie(refreshTokenCookie);

            String redirectUrl = successRedirectUrl + "/oauth/callback";
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("GitHub OAuth 로그인 처리 중 오류: {}", e.getMessage(), e);

            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            String errorRedirectUrl = successRedirectUrl + "?error=" + errorMessage + "&socialType=github";

            response.sendRedirect(errorRedirectUrl);
        }
    }

    private String extractGitHubProviderId(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        Object id = attributes.get("id");

        if (id == null) {
            throw ErrorCode.OAUTH_USER_REGISTRATION_FAILED.get();
        }

        return String.valueOf(id);
    }
}