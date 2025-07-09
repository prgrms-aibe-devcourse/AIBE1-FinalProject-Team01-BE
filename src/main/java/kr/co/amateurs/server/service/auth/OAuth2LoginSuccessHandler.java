package kr.co.amateurs.server.service.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.amateurs.server.config.auth.CookieUtils;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.TokenInfoDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;

    @Value("${oauth.success-redirect-url:http://localhost:5173}")
    private String successRedirectUrl;

    @Value("${cookie.domain:}")
    private String cookieDomain;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        ProviderType providerType = extractProviderFromRequest(request);

        log.debug("{} OAuth2User 로그인 성공 핸들러 진입", providerType.getProviderName());

        try {
            String providerId = extractProviderId(oAuth2User, providerType);

            User user = userRepository.findByProviderIdAndProviderType(providerId, providerType)
                    .orElseThrow(ErrorCode.USER_NOT_FOUND);

            log.info("{} 로그인 성공: userId={}, providerId={}", providerType.getProviderName(), user.getId(), providerId);

            String accessToken = jwtProvider.generateAccessToken(user.getEmail());
            Long accessExpiresIn = jwtProvider.getAccessTokenExpirationMs();

            String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
            Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs();

            refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken, refreshExpiresIn / 1000);

            TokenInfoDTO tokenInfoDTO = TokenInfoDTO.of(accessToken, accessExpiresIn, refreshToken, refreshExpiresIn);
            cookieUtils.setAuthTokenCookie(response, tokenInfoDTO);

            String redirectUrl = successRedirectUrl + "/oauth/callback";
            response.sendRedirect(redirectUrl);
        } catch (CustomException e) {
            log.warn("{} OAuth 로그인 실패: {}", providerType.getProviderName(), e.getMessage());
            redirectWithError(response, providerType, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("{} OAuth 로그인 실패 - 지원하지 않는 제공자: {}", providerType.getProviderName(), e.getMessage());
            redirectWithError(response, providerType, ErrorCode.OAUTH_INVALID_PROVIDER.get().getMessage());
        } catch (RuntimeException e) {
            log.error("{} OAuth 로그인 처리 중 시스템 오류: {}", providerType.getProviderName(), e.getMessage(), e);
            redirectWithError(response, providerType, ErrorCode.OAUTH_SYSTEM_ERROR.get().getMessage());
        } catch (IOException e) {
            log.error("{} OAuth 로그인 중 리다이렉트 오류: {}", providerType.getProviderName(), e.getMessage(), e);
            handleIOException(response, e);
        }
    }

    private String extractProviderId(OAuth2User oauth2User, ProviderType providerType) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        Object id = attributes.get("id");

        if (id == null) {
            log.error("{}에서 사용자 ID를 받지 못했습니다", providerType.getProviderName());
            throw ErrorCode.OAUTH_USER_REGISTRATION_FAILED.get();
        }

        return String.valueOf(id);
    }

    private ProviderType extractProviderFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        if (requestURI.contains("/oauth2/code/github")) {
            return ProviderType.GITHUB;
        } else if (requestURI.contains("/oauth2/code/kakao")) {
            return ProviderType.KAKAO;
        }

        return ProviderType.GITHUB;
    }

    private void redirectWithError(HttpServletResponse response, ProviderType providerType, String message) throws IOException {
        String errorMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String errorRedirectUrl = successRedirectUrl + "?error=" + errorMessage + "&socialType=" + providerType.getProviderName();
        response.sendRedirect(errorRedirectUrl);
    }

    private void handleIOException(HttpServletResponse response, IOException originalException) {
        try {
            CustomException ce = ErrorCode.OAUTH_REDIRECT_FAILED.get();
            response.sendError(ce.getErrorCode().getHttpStatus().value(), ce.getMessage());
        } catch (IOException ioException) {
            log.error("에러 응답 전송도 실패: {}", ioException.getMessage());
        }
    }
}