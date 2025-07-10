package kr.co.amateurs.server.config.auth;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.TokenInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CookieUtils {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final int BEARER_PREFIX_LENGTH = 7;
    private static final long MILLISECONDS_TO_SECONDS = 1000L;
    private static final int MAX_COOKIE_AGE = Integer.MAX_VALUE;

    @Value("${cookie.domain:}")
    private String cookieDomain;

    @Value("${cookie.secure:true}")
    private boolean secure;

    public void setAuthTokenCookie(HttpServletResponse response, TokenInfoDTO tokenInfoDTO) {
        log.debug("인증 토큰 쿠키 설정 시작");

        validateTokenInfo(tokenInfoDTO);
        validateResponse(response);

        try {
            int accessTokenMaxAge = convertToSafeMaxAge(tokenInfoDTO.accessTokenExpiresIn());
            String accessTokenCookie = buildSecureCookieString(
                    ACCESS_TOKEN_COOKIE_NAME,
                    tokenInfoDTO.accessToken(),
                    accessTokenMaxAge
            );
            response.addHeader("Set-Cookie", accessTokenCookie);
            log.debug("Access Token 쿠키 설정 완료");

            int refreshTokenMaxAge = convertToSafeMaxAge(tokenInfoDTO.refreshTokenExpiresIn());
            String refreshTokenCookie = buildSecureCookieString(
                    REFRESH_TOKEN_COOKIE_NAME,
                    tokenInfoDTO.refreshToken(),
                    refreshTokenMaxAge
            );
            response.addHeader("Set-Cookie", refreshTokenCookie);
            log.debug("Refresh Token 쿠키 설정 완료");

        } catch (Exception e) {
            log.error("쿠키 설정 중 오류 발생", e);
            throw ErrorCode.COOKIE_SETTING_FAILED.get();
        }
    }

    private String buildSecureCookieString(String name, String value, int maxAge) {
        StringBuilder cookie = new StringBuilder();

        String encodedValue = encodeCookieValue(value);
        cookie.append(name).append("=").append(encodedValue);

        cookie.append("; Path=/");
        cookie.append("; HttpOnly");

        if (secure) {
            cookie.append("; Secure");
        }

        cookie.append("; SameSite=Strict");

        if (StringUtils.hasText(cookieDomain)) {
            cookie.append("; Domain=").append(cookieDomain);
        }

        cookie.append("; Max-Age=").append(maxAge);

        return cookie.toString();
    }

    private int convertToSafeMaxAge(Long expiresInMs) {
        if (expiresInMs == null || expiresInMs <= 0) {
            log.warn("유효하지 않은 만료 시간: {}", expiresInMs);
            return 0;
        }

        long expiresInSeconds = expiresInMs / MILLISECONDS_TO_SECONDS;

        if (expiresInSeconds > MAX_COOKIE_AGE) {
            log.warn("토큰 만료 시간이 최대값을 초과합니다. 최대값으로 제한: {} -> {}",
                    expiresInSeconds, MAX_COOKIE_AGE);
            return MAX_COOKIE_AGE;
        }

        return (int) expiresInSeconds;
    }

    private String encodeCookieValue(String value) {
        if (value.contains(";") || value.contains(",") || value.contains(" ") || value.contains("\"")) {
            throw ErrorCode.INVALID_COOKIE_VALUE_FORMAT.get();
        }
        return value;
    }

    private void validateTokenInfo(TokenInfoDTO tokenInfoDTO) {
        if (tokenInfoDTO == null) {
            throw ErrorCode.INVALID_TOKEN_INFO.get();
        }

        if (!StringUtils.hasText(tokenInfoDTO.accessToken())) {
            throw ErrorCode.MISSING_ACCESS_TOKEN.get();
        }

        if (!StringUtils.hasText(tokenInfoDTO.refreshToken())) {
            throw ErrorCode.MISSING_REFRESH_TOKEN.get();
        }

        if (tokenInfoDTO.accessTokenExpiresIn() == null || tokenInfoDTO.accessTokenExpiresIn() <= 0) {
            throw ErrorCode.INVALID_EXPIRATION_TIME.get();
        }

        if (tokenInfoDTO.refreshTokenExpiresIn() == null || tokenInfoDTO.refreshTokenExpiresIn() <= 0) {
            throw ErrorCode.INVALID_EXPIRATION_TIME.get();
        }
    }

    private void validateResponse(HttpServletResponse response) {
        if (response == null) {
            throw ErrorCode.INVALID_HTTP_RESPONSE.get();
        }
    }

    public void clearAuthTokenCookie(HttpServletResponse response) {

        try {
            String accessTokenClearCookie = buildClearCookieString(ACCESS_TOKEN_COOKIE_NAME);
            response.addHeader("Set-Cookie", accessTokenClearCookie);

            String refreshTokenClearCookie = buildClearCookieString(REFRESH_TOKEN_COOKIE_NAME);
            response.addHeader("Set-Cookie", refreshTokenClearCookie);

        } catch (Exception e) {
            log.error("쿠키 삭제 중 오류 발생", e);
            throw ErrorCode.COOKIE_CLEAR_FAILED.get();
        }
    }

    private String buildClearCookieString(String name) {
        StringBuilder cookie = new StringBuilder();

        cookie.append(name).append("=");
        cookie.append("; Path=/");
        cookie.append("; HttpOnly");

        if (secure) {
            cookie.append("; Secure");
        }

        cookie.append("; SameSite=Strict");

        if (StringUtils.hasText(cookieDomain)) {
            cookie.append("; Domain=").append(cookieDomain);
        }

        cookie.append("; Max-Age=0");

        return cookie.toString();
    }
}