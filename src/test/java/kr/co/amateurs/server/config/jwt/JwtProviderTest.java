package kr.co.amateurs.server.config.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        String testSecret = Base64.getEncoder().encodeToString(
                "test-secret-key-for-jwt-must-be-at-least-256-bits-long".getBytes()
        );
        jwtProvider = new JwtProvider(testSecret, 3600000L, 1209600000L);
    }

    @Test
    void 정상적인_토큰_검증_시_true를_반환한다() {
        // given
        String email = "test@test.com";
        String token = jwtProvider.generateAccessToken(email);

        // when
        boolean result = jwtProvider.validateToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 잘못된_형식의_토큰_검증_시_false를_반환한다() {
        // given
        String invalidToken = "invalid.token.format";

        // when
        boolean result = jwtProvider.validateToken(invalidToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 빈_토큰_검증_시_false를_반환한다() {
        // given
        String emptyToken = "";

        // when
        boolean result = jwtProvider.validateToken(emptyToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void null_토큰_검증_시_false를_반환한다() {
        // given
        String nullToken = null;

        // when
        boolean result = jwtProvider.validateToken(nullToken);

        // then
        assertThat(result).isFalse();
    }
}