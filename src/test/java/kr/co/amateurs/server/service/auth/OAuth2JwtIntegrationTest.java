package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.entity.auth.RefreshToken;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.fixture.auth.OAuth2TestFixture;
import kr.co.amateurs.server.repository.user.UserRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(EmbeddedRedisConfig.class)
public class OAuth2JwtIntegrationTest {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        userRepository.deleteAll();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void OAuth_로그인_성공_후_JWT_토큰_생성_및_Redis_저장이_완료된다() {
        // given
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@github.com";
        String githubApiResponse = OAuth2TestFixture.createGitHubApiResponse(
                "12345", "testuser", "테스트유저", uniqueEmail
        );
        OAuth2TestFixture.enqueueMockResponse(mockWebServer, githubApiResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

        // when: OAuth
        OAuth2User oAuth2User = customOAuth2UserService.loadUser(userRequest);

        // then: 사용자 생성
        assertThat(oAuth2User).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails userDetails = (CustomUserDetails) oAuth2User;
        User savedUser = userDetails.getUser();

        assertThat(savedUser.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedUser.getProviderType()).isEqualTo(ProviderType.GITHUB);
        assertThat(savedUser.getProviderId()).isEqualTo("12345");

        // when: JWT
        String accessToken = jwtProvider.generateAccessToken(savedUser.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(savedUser.getEmail());
        Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs() / 1000;

        // then: JWT
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        assertThat(jwtProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtProvider.getEmailFromToken(accessToken)).isEqualTo(uniqueEmail);
        assertThat(jwtProvider.getEmailFromToken(refreshToken)).isEqualTo(uniqueEmail);

        // when: RefreshToken
        refreshTokenService.saveRefreshToken(savedUser.getEmail(), refreshToken, refreshExpiresIn);

        // then: RefreshToken
        Optional<RefreshToken> savedRefreshToken = refreshTokenService.findByEmail(uniqueEmail);
        assertThat(savedRefreshToken).isPresent();
        assertThat(savedRefreshToken.get().getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedRefreshToken.get().getExpiration()).isEqualTo(refreshExpiresIn);

        String emailFromToken = jwtProvider.getEmailFromToken(accessToken);
        Optional<User> userByEmail = userRepository.findByEmail(emailFromToken);
        assertThat(userByEmail).isPresent();
        assertThat(userByEmail.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void 기존_OAuth_사용자_로그인_시_새로운_JWT_토큰이_생성된다() {
        // given
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@github.com";
        User existingUser = OAuth2TestFixture.defaultGitHubUser()
                .providerId("12345")
                .email(uniqueEmail)
                .nickname("testuser")
                .name("테스트유저")
                .build();
        userRepository.save(existingUser);

        String oldRefreshToken = jwtProvider.generateRefreshToken(existingUser.getEmail());
        refreshTokenService.saveRefreshToken(existingUser.getEmail(), oldRefreshToken, 3600L);

        String githubApiResponse = OAuth2TestFixture.createGitHubApiResponse(
                "12345", "testuser", "테스트유저", uniqueEmail
        );
        OAuth2TestFixture.enqueueMockResponse(mockWebServer, githubApiResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

        // when
        OAuth2User oAuth2User = customOAuth2UserService.loadUser(userRequest);
        CustomUserDetails userDetails = (CustomUserDetails) oAuth2User;
        User loginUser = userDetails.getUser();

        String newAccessToken = jwtProvider.generateAccessToken(loginUser.getEmail());
        String newRefreshToken = jwtProvider.generateRefreshToken(loginUser.getEmail());
        Long refreshExpiresIn = jwtProvider.getRefreshTokenExpirationMs() / 1000;

        refreshTokenService.saveRefreshToken(loginUser.getEmail(), newRefreshToken, refreshExpiresIn);


        // then
        assertThat(newAccessToken).isNotNull();
        assertThat(newRefreshToken).isNotNull();
        assertThat(jwtProvider.validateToken(newAccessToken)).isTrue();
        assertThat(jwtProvider.validateToken(newRefreshToken)).isTrue();

        assertThat(jwtProvider.getEmailFromToken(newAccessToken)).isEqualTo(uniqueEmail);

        Optional<RefreshToken> updatedRefreshToken = refreshTokenService.findByEmail(uniqueEmail);
        assertThat(updatedRefreshToken).isPresent();
        assertThat(updatedRefreshToken.get().getEmail()).isEqualTo(uniqueEmail);

        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1);
    }

    @Test
    void 여러_OAuth_사용자의_JWT_토큰이_독립적으로_관리된다() {
        // given - User1
        long timestamp = System.currentTimeMillis();
        String email1 = "testuser" + timestamp + "@github.com";
        String response1 = OAuth2TestFixture.createGitHubApiResponse(
                "12345", "testuser", "테스트유저", email1
        );
        OAuth2TestFixture.enqueueMockResponse(mockWebServer, response1);
        OAuth2UserRequest userRequest1 = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);
        User user1 = ((CustomUserDetails) customOAuth2UserService.loadUser(userRequest1)).getUser();
        String token1 = jwtProvider.generateAccessToken(user1.getEmail());

        // given - User2
        String email2 = "testuser" + (timestamp + 1) + "@github.com";
        String response2 = OAuth2TestFixture.createGitHubApiResponse(
                "67890", "testuser2", "테스트유저2", email2
        );
        OAuth2TestFixture.enqueueMockResponse(mockWebServer, response2);
        OAuth2UserRequest userRequest2 = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);
        User user2 = ((CustomUserDetails) customOAuth2UserService.loadUser(userRequest2)).getUser();
        String token2 = jwtProvider.generateAccessToken(user2.getEmail());

        // then
        assertThat(jwtProvider.getEmailFromToken(token1)).isEqualTo(email1);
        assertThat(jwtProvider.getEmailFromToken(token2)).isEqualTo(email2);
        assertThat(token1).isNotEqualTo(token2);

        assertThat(user1.getId()).isNotEqualTo(user2.getId());
        assertThat(user1.getProviderId()).isEqualTo("12345");
        assertThat(user2.getProviderId()).isEqualTo("67890");
        assertThat(userRepository.count()).isEqualTo(2);
    }
}
