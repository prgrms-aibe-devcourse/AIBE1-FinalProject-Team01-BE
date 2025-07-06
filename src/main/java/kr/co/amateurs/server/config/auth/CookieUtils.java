package kr.co.amateurs.server.config.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.amateurs.server.domain.dto.auth.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class CookieUtils {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final int BEARER_PREFIX_LENGTH = 7;

    @Value("${cookie.domain:}")
    private String cookieDomain;

    @Value("${cookie.secure:true}")
    private boolean secure;

    public void setAuthTokenCookie(HttpServletResponse response, TokenInfo tokenInfo) {
        log.debug("인증 토큰 쿠키 설정 시작");

        Cookie accessTokenCookie = createSecureCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                tokenInfo.accessToken(),
                Math.toIntExact(tokenInfo.accessTokenExpiresIn() / 1000)
        );
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 생성 (초 단위)
        Cookie refreshTokenCookie = createSecureCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                tokenInfo.refreshToken(),
                Math.toIntExact(tokenInfo.refreshTokenExpiresIn() / 1000)
        );
        response.addCookie(refreshTokenCookie);
    }

    private Cookie createSecureCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);

        if (StringUtils.hasText(cookieDomain)) {
            cookie.setDomain(cookieDomain);
        }

        return cookie;
    }
}
