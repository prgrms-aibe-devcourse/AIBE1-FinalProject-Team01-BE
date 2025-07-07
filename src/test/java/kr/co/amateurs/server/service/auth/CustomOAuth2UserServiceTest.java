package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.auth.OAuth2TestFixture;  // ✨ Fixture import
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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void 신규_GitHub_사용자는_회원가입이_가능하다() {
        String githubApiResponse = OAuth2TestFixture.createGitHubApiResponse(
                "12345", "newuser", "뉴깃헙", "newuser@github.com"
        );

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, githubApiResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

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

    @Test
    void 기존_GitHub_사용자는_로그인이_가능하다() {
        User existingUser = OAuth2TestFixture.defaultGitHubUser()
                .providerId("12345")
                .email("newuser@github.com")
                .nickname("newuser_abc123")
                .name("뉴깃헙")
                .imageUrl("https://github.com/avatar.jpg")
                .build();
        userRepository.save(existingUser);

        String githubApiResponse = OAuth2TestFixture.createGitHubApiResponse(
                "12345", "newuser", "뉴깃헙", "newuser@github.com"
        );

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, githubApiResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

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

    @Test
    void GitHub에서_사용자_ID가_없으면_예외가_발생한다() {
        String responseWithoutId = OAuth2TestFixture.createGitHubApiResponseWithoutId();

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, responseWithoutId);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void GitHub에서_로그인_정보가_없으면_예외가_발생한다() {
        String responseWithoutLogin = OAuth2TestFixture.createGitHubApiResponseWithoutLogin();

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, responseWithoutLogin);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("login");
    }

    @Test
    void 다른_계정으로_이미_가입된_이메일로_가입_시도하면_예외가_발생한다() {
        User existingUser = OAuth2TestFixture.defaultGitHubUser()
                .providerId("existing123")
                .email("duplicate@test.com")
                .nickname("existing_user")
                .name("Existing User")
                .imageUrl("https://existing.jpg")
                .build();
        userRepository.save(existingUser);

        String duplicateEmailResponse = OAuth2TestFixture.createGitHubApiResponse(
                "99999", "newuser", "같은메일사용자", "duplicate@test.com"
        );

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, duplicateEmailResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(OAuth2AuthenticationException.class);

        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1);
    }

    @Test
    void GitHub에서_이메일이_없으면_가짜_이메일이_생성된다() {
        String responseWithoutEmail = OAuth2TestFixture.createGitHubApiResponseWithoutEmail(
                "55555", "noemail", "메일없는사용자"
        );

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, responseWithoutEmail);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createGitHubOAuth2UserRequest(mockWebServer);

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(CustomUserDetails.class);

        Optional<User> savedUser = userRepository.findByProviderIdAndProviderType("55555", ProviderType.GITHUB);
        assertThat(savedUser).isPresent();

        String generatedEmail = savedUser.get().getEmail();
        assertThat(generatedEmail).startsWith("github_55555@");
        assertThat(generatedEmail).endsWith("@amateurs.com");
        assertThat(savedUser.get().getName()).isEqualTo("메일없는사용자");
        assertThat(savedUser.get().getNickname()).startsWith("noemail_");
    }
}