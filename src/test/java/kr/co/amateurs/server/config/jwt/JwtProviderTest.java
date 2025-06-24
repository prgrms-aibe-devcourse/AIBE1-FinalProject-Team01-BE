package kr.co.amateurs.server.config.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void 액세스_토큰을_생성하고_검증할_수_있다() {
        // given
        String email = "test@test.com";

        // when
        String accessToken = jwtProvider.generateAccessToken(email);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
        assertThat(jwtProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtProvider.getEmailFromToken(accessToken)).isEqualTo(email);
    }

    @Test
    void 잘못된_형식의_토큰은_검증에_실패한다() {
        // given
        String invalidToken = "invalid.token.format";

        // when
        boolean result = jwtProvider.validateToken(invalidToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 빈_토큰은_검증에_실패한다() {
        // given
        String emptyToken = "";

        // when
        boolean result = jwtProvider.validateToken(emptyToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void null_토큰은_검증에_실패한다() {
        // given
        String nullToken = null;

        // when
        boolean result = jwtProvider.validateToken(nullToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 액세스_토큰_만료_시간을_반환할_수_있다() {
        // when
        Long expirationMs = jwtProvider.getAccessTokenExpirationMs();

        // then
        assertThat(expirationMs).isEqualTo(3600000L);
    }

    @Test
    void 리프레시_토큰을_생성하고_검증할_수_있다() {
        // given
        String email = "test@test.com";

        // when
        String refreshToken = jwtProvider.generateRefreshToken(email);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(jwtProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtProvider.getEmailFromToken(refreshToken)).isEqualTo(email);
    }

    @Test
    void 액세스_토큰과_리프레시_토큰은_서로_다른_토큰이다() {
        // given
        String email = "test@test.com";

        // when
        String accessToken = jwtProvider.generateAccessToken(email);
        String refreshToken = jwtProvider.generateRefreshToken(email);

        // then
        assertThat(accessToken).isNotEqualTo(refreshToken);
        assertThat(jwtProvider.getEmailFromToken(accessToken)).isEqualTo(email);
        assertThat(jwtProvider.getEmailFromToken(refreshToken)).isEqualTo(email);
    }

    @Test
    void 액세스_토큰과_리프레시_토큰의_만료_시간이_다르다() {
        // when
        Long accessExpirationMs = jwtProvider.getAccessTokenExpirationMs();
        Long refreshExpirationMs = jwtProvider.getRefreshTokenExpirationMs();

        // then
        assertThat(accessExpirationMs).isLessThan(refreshExpirationMs);
        assertThat(refreshExpirationMs).isEqualTo(1209600000L);
    }
}