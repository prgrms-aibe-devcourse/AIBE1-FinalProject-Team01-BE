package kr.co.amateurs.server.config.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.amateurs.server.domain.dto.auth.TokenInfoDTO;
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

    public void setAuthTokenCookie(HttpServletResponse response, TokenInfoDTO tokenInfoDTO) {
        log.debug("인증 토큰 쿠키 설정 시작");

        String accessTokenCookie = buildCookieString(
                ACCESS_TOKEN_COOKIE_NAME,
                tokenInfoDTO.accessToken(),
                Math.toIntExact(tokenInfoDTO.accessTokenExpiresIn() / 1000)
        );
        response.addHeader("Set-Cookie", accessTokenCookie);

        String refreshTokenCookie = buildCookieString(
                REFRESH_TOKEN_COOKIE_NAME,
                tokenInfoDTO.refreshToken(),
                Math.toIntExact(tokenInfoDTO.refreshTokenExpiresIn() / 1000)
        );
        response.addHeader("Set-Cookie", refreshTokenCookie);
    }

    private String buildCookieString(String name, String value, int maxAge) {
        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append("=").append(value);
        cookie.append("; Path=/; HttpOnly");

        if (secure) {
            cookie.append("; Secure");
        }

        if (StringUtils.hasText(cookieDomain)) {
            cookie.append("; Domain=").append(cookieDomain);
        }

        cookie.append("; Max-Age=").append(maxAge);

        return cookie.toString();
    }
}
