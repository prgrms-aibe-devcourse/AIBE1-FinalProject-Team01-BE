package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.config.jwt.JwtProvider;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    private JwtProvider jwtProvider;

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

    @Test
    void 기존_카카오_사용자는_로그인이_가능하다() {
        // given
        User existingUser = OAuth2TestFixture.defaultKakaoUser()
                .providerId("123456789")
                .email("existing@kakao.com")
                .nickname("기존카카오_abc123")
                .name("기존카카오")
                .build();
        userRepository.save(existingUser);

        String kakaoApiResponse = OAuth2TestFixture.createKakaoApiResponse(
                "123456789", "existing@kakao.com", "기존카카오"
        );

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, kakaoApiResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createKakaoOAuth2UserRequest(mockWebServer);

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails userDetails = (CustomUserDetails) result;
        User returnedUser = userDetails.getUser();
        assertThat(returnedUser.getEmail()).isEqualTo("existing@kakao.com");
        assertThat(returnedUser.getNickname()).isEqualTo("기존카카오_abc123");

        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1);
    }

    @Test
    void 카카오에서_사용자_ID가_없으면_예외가_발생한다() {
        // given
        String kakaoApiResponseWithoutId = OAuth2TestFixture.createKakaoApiResponseWithoutId();

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, kakaoApiResponseWithoutId);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createKakaoOAuth2UserRequest(mockWebServer);

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 카카오_OAuth_로그인_후_JWT_토큰_생성이_완료된다() {
        // given
        String uniqueEmail = "kakaotest" + System.currentTimeMillis() + "@kakao.com";
        String kakaoApiResponse = OAuth2TestFixture.createKakaoApiResponse(
                "789456123", uniqueEmail, "카카오테스트"
        );

        OAuth2TestFixture.enqueueMockResponse(mockWebServer, kakaoApiResponse);
        OAuth2UserRequest userRequest = OAuth2TestFixture.createKakaoOAuth2UserRequest(mockWebServer);

        // when: OAuth 로그인
        OAuth2User oAuth2User = customOAuth2UserService.loadUser(userRequest);

        // then: 사용자 생성
        assertThat(oAuth2User).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails userDetails = (CustomUserDetails) oAuth2User;
        User savedUser = userDetails.getUser();

        assertThat(savedUser.getEmail()).isEqualTo(uniqueEmail);
        assertThat(savedUser.getProviderType()).isEqualTo(ProviderType.KAKAO);
        assertThat(savedUser.getProviderId()).isEqualTo("789456123");

        // when: JWT
        String accessToken = jwtProvider.generateAccessToken(savedUser.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(savedUser.getEmail());

        // then: JWT
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        assertThat(jwtProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtProvider.getEmailFromToken(accessToken)).isEqualTo(uniqueEmail);
    }
}