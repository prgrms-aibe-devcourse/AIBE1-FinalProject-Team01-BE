package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.fixture.auth.OAuth2TestFixture;
import kr.co.amateurs.server.repository.user.UserRepository;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(EmbeddedRedisConfig.class)
public class KakaoOAuth2UserServiceTest {

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
    void 신규_카카오_사용자는_회원가입이_가능하다() {
        // given
        String kakaoApiResponse = OAuth2TestFixture.createKakaoApiResponse(
                "123456789", "newkakao@kakao.com", "김카카오"
        );

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, kakaoApiResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createKakaoOAuth2UserRequest(mockWebServer);

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(CustomUserDetails.class);

        Optional<User> savedUser = userRepository.findByProviderIdAndProviderType(
                "123456789", ProviderType.KAKAO
        );
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("newkakao@kakao.com");
        assertThat(savedUser.get().getProviderType()).isEqualTo(ProviderType.KAKAO);
        assertThat(savedUser.get().getProviderId()).isEqualTo("123456789");
        assertThat(savedUser.get().getRole()).isEqualTo(Role.GUEST);
        assertThat(savedUser.get().getName()).isEqualTo("김카카오");
        assertThat(savedUser.get().getNickname()).startsWith("김카카오_");
    }
}