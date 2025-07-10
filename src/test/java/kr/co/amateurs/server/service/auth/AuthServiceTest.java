package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.dto.auth.*;
import kr.co.amateurs.server.config.TestAuthHelper;
import kr.co.amateurs.server.fixture.auth.TokenTestFixture;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDTO;
import kr.co.amateurs.server.domain.dto.auth.LoginResponseDTO;
import kr.co.amateurs.server.domain.dto.auth.SignupRequestDTO;
import kr.co.amateurs.server.domain.dto.auth.SignupResponseDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(EmbeddedRedisConfig.class)
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void 정상적인_유저_등록_시_회원가입이_성공한다() {
        // given
        SignupRequestDTO request = UserTestFixture.createUniqueSignupRequest();

        // when
        SignupResponseDTO response = authService.signup(request);

        // then
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.nickname()).isEqualTo(request.nickname());
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.topics()).isEqualTo(request.topics());
        assertThat(response.userId()).isNotNull();

        Optional<User> savedUser = userRepository.findByEmail(request.email());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo(request.email());
    }

    @Test
    void 중복된_이메일로_회원가입_시_예외가_발생한다() {
        // given
        SignupRequestDTO firstRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(firstRequest);

        SignupRequestDTO duplicateEmailRequest = UserTestFixture.defaultSignupRequest()
                .email(firstRequest.email())
                .nickname(UserTestFixture.generateUniqueNickname())
                .build();

        // when & then
        assertThatThrownBy(() -> authService.signup(duplicateEmailRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 중복된_닉네임으로_회원가입_시_예외가_발생한다() {
        // given
        SignupRequestDTO firstRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(firstRequest);

        SignupRequestDTO duplicateNicknameRequest = UserTestFixture.defaultSignupRequest()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(firstRequest.nickname())
                .build();

        // when & then
        assertThatThrownBy(() -> authService.signup(duplicateNicknameRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 회원가입_시_비밀번호가_실제로_BCrypt_암호화된다() {
        // given
        SignupRequestDTO request = UserTestFixture.createUniqueSignupRequest();

        // when
        SignupResponseDTO response = authService.signup(request);

        // then
        Optional<User> savedUser = userRepository.findByEmail(request.email());
        assertThat(savedUser).isPresent();

        String actualEncryptedPassword = savedUser.get().getPassword();

        assertThat(passwordEncoder.matches(request.password(), actualEncryptedPassword)).isTrue();
        assertThat(actualEncryptedPassword).isNotEqualTo(request.password());
        assertThat(actualEncryptedPassword).startsWith("$2a$");
        assertThat(actualEncryptedPassword.length()).isGreaterThan(50);
    }

    @Test
    void 토픽이_올바르게_User에_설정되어_저장된다() {
        // given
        Set<Topic> topics = Set.of(Topic.FRONTEND, Topic.BACKEND, Topic.AI);
        SignupRequestDTO request = UserTestFixture.defaultSignupRequest()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(UserTestFixture.generateUniqueNickname())
                .topics(topics)
                .build();

        // when
        SignupResponseDTO response = authService.signup(request);

        // then
        assertThat(response.topics()).hasSize(3);
        assertThat(response.topics()).containsExactlyInAnyOrder(Topic.FRONTEND, Topic.BACKEND, Topic.AI);
    }

    @Test
    void 토픽_4개_이상_선택_시_validation_에러가_발생한다() {
        // given
        Set<Topic> tooManyTopics = Set.of(
                Topic.FRONTEND, Topic.BACKEND, Topic.AI, Topic.MOBILE
        );

        SignupRequestDTO request = SignupRequestDTO.builder()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(UserTestFixture.generateUniqueNickname())
                .name("김테스트")
                .password(TokenTestFixture.getTestPassword())
                .topics(tooManyTopics)
                .build();

        // when
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<SignupRequestDTO>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("토픽은 1개 이상 3개 이하로 선택해주세요");
        }
    }

    @Test
    void 정상적인_로그인_시_JWT_토큰을_반환한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(signupRequest);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();

        // when
        LoginResponseDTO response = authService.login(loginRequest);

        // then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.tokenType()).isEqualTo(TokenTestFixture.TOKEN_TYPE);
        assertThat(response.expiresIn()).isEqualTo(TokenTestFixture.ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    void 존재하지_않는_이메일로_로그인_시_예외가_발생한다() {
        // given
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email(TokenTestFixture.getNonExistentEmail())
                .password(TokenTestFixture.getTestPassword())
                .build();

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 잘못된_비밀번호로_로그인_시_예외가_발생한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(signupRequest);

        LoginRequestDTO wrongPasswordRequest = LoginRequestDTO.builder()
                .email(signupRequest.email())
                .password(TokenTestFixture.getWrongPassword())
                .build();

        // when & then
        assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 정상적인_로그인_시_리프레시_토큰도_함께_반환한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(signupRequest);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();

        // when
        LoginResponseDTO response = authService.login(loginRequest);

        // then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
        assertThat(response.tokenType()).isEqualTo(TokenTestFixture.TOKEN_TYPE);
        assertThat(response.expiresIn()).isEqualTo(TokenTestFixture.ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    void 유효한_리프레시_토큰으로_토큰_재발급_시_새로운_액새스_토큰을_반환한다() throws InterruptedException {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(signupRequest);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();

        LoginResponseDTO loginResponse = authService.login(loginRequest);
        Thread.sleep(1000);

        TokenReissueRequestDTO reissueRequest = new TokenReissueRequestDTO(loginResponse.refreshToken());

        // when
        TokenReissueResponseDTO response = authService.reissueToken(reissueRequest);

        // then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(TokenTestFixture.ACCESS_TOKEN_EXPIRATION);
        assertThat(response.accessToken()).isNotEqualTo(loginResponse.accessToken());
    }

    @Test
    void 유효하지_않은_리프레시_토큰으로_재발급_시_예외가_발생한다() {
        // given
        TokenReissueRequestDTO invalidRequest = new TokenReissueRequestDTO("invalid.token.here");

        // when & then
        assertThatThrownBy(() -> authService.reissueToken(invalidRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 존재하지_않는_사용자의_리프레시_토큰으로_재발급_시_예외가_발생한다() {
        // given
        String fakeRefreshToken = jwtProvider.generateRefreshToken("nonexistent@test.com");
        TokenReissueRequestDTO request = new TokenReissueRequestDTO(fakeRefreshToken);

        // when & then
        assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 정상적인_로그아웃_시_리프레시_토큰이_삭제된다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        User saveUser = TestAuthHelper.setAuthentication(
                UserTestFixture.defaultUser()
                        .email(signupRequest.email())
                        .nickname(signupRequest.nickname())
                        .password(passwordEncoder.encode(signupRequest.password()))
                        .build(),
                userRepository
        );

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();
        authService.login(loginRequest);

        assertThat(refreshTokenService.existsByEmail(saveUser.getEmail())).isTrue();

        // when
        authService.logout(null);

        // then
        assertThat(refreshTokenService.existsByEmail(saveUser.getEmail())).isFalse();
    }

    @Test
    void 인증되지_않은_사용자가_로그아웃_시도_시_예외가_발생한다() {
        // given
        TestAuthHelper.clearAuthentication();

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.logout(null);
        });

        assertThat(exception.getMessage()).isEqualTo("로그인이 필요합니다.");
    }
}