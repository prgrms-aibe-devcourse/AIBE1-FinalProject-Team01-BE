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
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${oauth.success-redirect-url:http://localhost:5173}")
    private String successRedirectUrl;

    @Value("${cookie.domain:}")
    private String cookieDomain;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String provider  = extractProviderFromRequest(request);

        log.debug("{} OAuth2User 로그인 성공 핸들러 진입", provider);

        try {
            String providerId = extractProviderId(oAuth2User, provider);

            ProviderType providerType = ProviderType.valueOf(provider.toUpperCase());

            User user = userRepository.findByProviderIdAndProviderType(providerId, providerType)
                    .orElseThrow(ErrorCode.USER_NOT_FOUND);

            log.info("{} 로그인 성공: userId={}, providerId={}", provider, user.getId(), providerId);

            String accessToken = jwtProvider.generateAccessToken(user.getEmail());
            Long accessExpiresIn = jwtProvider.getAccessTokenExpirationMs();

            String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
            Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs() / 1000;

            refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken, refreshExpiresIn);

            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setMaxAge(Math.toIntExact(accessExpiresIn / 1000));
            if (StringUtils.hasText(cookieDomain)) {
                accessTokenCookie.setDomain(cookieDomain);
            }
            accessTokenCookie.setPath("/");
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setHttpOnly(true);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setMaxAge(Math.toIntExact(refreshExpiresIn));
            if (StringUtils.hasText(cookieDomain)) {
                refreshTokenCookie.setDomain(cookieDomain);
            }
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setHttpOnly(true);
            response.addCookie(refreshTokenCookie);

            String redirectUrl = successRedirectUrl + "/oauth/callback";
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("{} OAuth 로그인 처리 중 오류: {}", provider, e.getMessage(), e);

            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            String errorRedirectUrl = successRedirectUrl + "?error=" + errorMessage + "&socialType=" + provider;

            response.sendRedirect(errorRedirectUrl);
        }
    }

    private String extractProviderId(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        Object id = attributes.get("id");

        if (id == null) {
            log.error("{}에서 사용자 ID를 받지 못했습니다", provider);
            throw ErrorCode.OAUTH_USER_REGISTRATION_FAILED.get();
        }

        return String.valueOf(id);
    }

    private String extractProviderFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        if (requestURI.contains("/oauth2/code/github")) {
            return "github";
        } else if (requestURI.contains("/oauth2/code/kakao")) {
            return "kakao";
        }

        return "github";
    }
}