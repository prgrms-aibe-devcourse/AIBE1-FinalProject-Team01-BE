package kr.co.amateurs.server.config.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class CookieUtils {

    @Value("${cookie.domain:}")
    private String cookieDomain;

    @Value("${cookie.secure:true}")
    private boolean secure;

    public void setAuthTokenCookie(HttpServletResponse response,
                                   String accessToken,
                                   Long accessTokenExpiresIn,
                                   String refreshToken,
                                   Long refreshTokenExpiresIn) {
        log.debug("인증 토큰 쿠키 설정 시작");

        Cookie accessTokenCookie = createSecureCookie("accessToken", accessToken,
                Math.toIntExact(accessTokenExpiresIn / 1000));
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = createSecureCookie("refreshToken", refreshToken,
                Math.toIntExact(refreshTokenExpiresIn));
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
