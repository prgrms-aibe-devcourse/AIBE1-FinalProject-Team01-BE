package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.entity.auth.RefreshToken;
import kr.co.amateurs.server.repository.auth.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@Import({RefreshTokenService.class, JwtProvider.class})
@ActiveProfiles("test")
public class RefreshTokenIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    void 리프레시_토큰을_저장하고_조회할_수_있다() {
        // given
        String email = "test@test.com";
        String token = "sample-refresh-token-123";
        Long expiration = 3600L;

        // when
        refreshTokenService.saveRefreshToken(email, token, expiration);

        // then
        Optional<RefreshToken> foundToken = refreshTokenService.findByEmail(email);

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getEmail()).isEqualTo(email);
        assertThat(foundToken.get().getToken()).isEqualTo(token);
        assertThat(foundToken.get().getExpiration()).isEqualTo(expiration);
    }

    @Test
    void 같은_이메일로_저장하면_기존_토큰이_덮어써진다() {
        // given
        String email = "test@test.com";
        String oldToken = "old-token-123";
        String newToken = "new-token-456";
        Long expiration = 3600L;

        // when
        refreshTokenService.saveRefreshToken(email, oldToken, expiration);
        refreshTokenService.saveRefreshToken(email, newToken, expiration);

        // then
        Optional<RefreshToken> foundToken = refreshTokenService.findByEmail(email);

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo(newToken);
        assertThat(foundToken.get().getToken()).isNotEqualTo(oldToken);
    }

    @Test
    void 존재하지_않는_이메일로_조회하면_빈_결과가_반환된다() {
        // given
        String email = "nonexistent@test.com";

        // when
        Optional<RefreshToken> foundToken = refreshTokenService.findByEmail(email);

        // then
        assertThat(foundToken).isEmpty();
    }

    @Test
    void 리프레시_토큰을_삭제할_수_있다() {
        // given
        String email = "delete@test.com";
        String token = "delete-token-123";
        Long expiration = 3600L;

        refreshTokenService.saveRefreshToken(email, token, expiration);

        // when
        refreshTokenService.deleteByEmail(email);

        // then
        assertThat(refreshTokenService.findByEmail(email)).isEmpty();
    }

    @Test
    void 리프레시_토큰_존재_여부를_확인할_수_있다() {
        // given
        String email = "exists@test.com";
        String token = "exists-token-123";
        Long expiration = 3600L;

        // when & then
        assertThat(refreshTokenService.existsByEmail(email)).isFalse();

        refreshTokenService.saveRefreshToken(email, token, expiration);
        assertThat(refreshTokenService.existsByEmail(email)).isTrue();

        refreshTokenService.deleteByEmail(email);
        assertThat(refreshTokenService.existsByEmail(email)).isFalse();
    }

    @Test
    void 여러_사용자의_리프레시_토큰을_독립적으로_관리할_수_있다() {
        // given
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";
        String token1 = "token-user1-123";
        String token2 = "token-user2-456";
        Long expiration = 3600L;

        // when
        refreshTokenService.saveRefreshToken(email1, token1, expiration);
        refreshTokenService.saveRefreshToken(email2, token2, expiration);

        // then
        Optional<RefreshToken> foundToken1 = refreshTokenService.findByEmail(email1);
        Optional<RefreshToken> foundToken2 = refreshTokenService.findByEmail(email2);

        assertThat(foundToken1).isPresent();
        assertThat(foundToken1.get().getToken()).isEqualTo(token1);

        assertThat(foundToken2).isPresent();
        assertThat(foundToken2.get().getToken()).isEqualTo(token2);

        // 한 사용자 삭제가 다른 사용자에게 영향 없음
        refreshTokenService.deleteByEmail(email1);

        assertThat(refreshTokenService.findByEmail(email1)).isEmpty();
        assertThat(refreshTokenService.findByEmail(email2)).isPresent();
    }

    @Test
    void 로그인_시_실제_JWT_리프레시_토큰이_생성되고_Redis에_저장된다() {
        // given
        String email = "auth-test@test.com";
        String token = jwtProvider.generateRefreshToken(email);
        Long expiration = jwtProvider.getRefreshTokenExpirationMs() / 1000;

        // when
        refreshTokenService.saveRefreshToken(email, token, expiration);

        // then
        Optional<RefreshToken> savedToken = refreshTokenService.findByEmail(email);

        assertThat(savedToken).isPresent();
        assertThat(savedToken.get().getToken()).isEqualTo(token);
        assertThat(savedToken.get().getEmail()).isEqualTo(email);
        assertThat(savedToken.get().getExpiration()).isEqualTo(expiration);

        assertThat(jwtProvider.validateToken(savedToken.get().getToken())).isTrue();
        assertThat(jwtProvider.getEmailFromToken(savedToken.get().getToken())).isEqualTo(email);

    }
}
