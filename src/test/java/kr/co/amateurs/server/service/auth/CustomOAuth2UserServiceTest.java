package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
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
public class CustomOAuth2UserServiceTest {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

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
    void 신규_GitHub_사용자는_회원가입이_가능하다() throws IOException {
        // given
        String githubApiResponse = """
            {
                "id": 12345,
                "login": "newuser",
                "name": "뉴깃헙",
                "email": "newuser@github.com",
                "avatar_url": "https://github.com/avatar.jpg"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(githubApiResponse)
                .addHeader("Content-Type", "application/json"));

        OAuth2UserRequest userRequest = createGitHubOAuth2UserRequest();

        System.out.println("Mock Server URL: " + mockWebServer.url("/"));
        System.out.println("UserInfo URI: " + userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri());

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(CustomUserDetails.class);

        Optional<User> savedUser = userRepository.findByProviderIdAndProviderType("12345", ProviderType.GITHUB);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("newuser@github.com");
        assertThat(savedUser.get().getProviderType()).isEqualTo(ProviderType.GITHUB);
        assertThat(savedUser.get().getProviderId()).isEqualTo("12345");
        assertThat(savedUser.get().getRole()).isEqualTo(Role.GUEST);
        assertThat(savedUser.get().getName()).isEqualTo("뉴깃헙");
        assertThat(savedUser.get().getNickname()).startsWith("newuser_");
    }

    private OAuth2UserRequest createGitHubOAuth2UserRequest() {
        String mockServerUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationUri(mockServerUrl + "/login/oauth/authorize")
                .tokenUri(mockServerUrl + "/login/oauth/access_token")
                .userInfoUri(mockServerUrl + "/user")
                .userNameAttributeName("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/github")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now(),
                Instant.now().plusMillis(3600000)
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }

    @Test
    void 기존_GitHub_사용자는_로그인이_가능하다() throws IOException {
        // given
        User existingUser = User.builder()
                .providerId("12345")
                .providerType(ProviderType.GITHUB)
                .email("newuser@github.com")
                .nickname("newuser_abc123")
                .name("뉴깃헙")
                .imageUrl("https://github.com/avatar.jpg")
                .role(Role.GUEST)
                .build();
        userRepository.save(existingUser);

        String githubApiResponse = """
        {
            "id": 12345,
            "login": "newuser",
            "name": "뉴깃헙",
            "email": "newuser@github.com",
            "avatar_url": "https://github.com/avatar.jpg"
        }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(githubApiResponse)
                .addHeader("Content-Type", "application/json"));

        OAuth2UserRequest userRequest = createGitHubOAuth2UserRequest();

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails userDetails = (CustomUserDetails) result;
        User returnedUser = userDetails.getUser();
        assertThat(returnedUser.getEmail()).isEqualTo("newuser@github.com");
        assertThat(returnedUser.getNickname()).isEqualTo("newuser_abc123");
        assertThat(returnedUser.getName()).isEqualTo("뉴깃헙");

        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1);
    }
}