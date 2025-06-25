package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.fixture.common.UserTestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDto;
import kr.co.amateurs.server.domain.dto.auth.LoginResponseDto;
import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.domain.dto.auth.SignupResponseDto;
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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void 정상적인_유저_등록_시_회원가입이_성공한다() {
        // given
        SignupRequestDto request = UserTestFixture.createUniqueSignupRequest();

        // when
        SignupResponseDto response = authService.signup(request);

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
        SignupRequestDto firstRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(firstRequest);

        SignupRequestDto duplicateEmailRequest = UserTestFixture.defaultSignupRequest()
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
        SignupRequestDto firstRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(firstRequest);

        SignupRequestDto duplicateNicknameRequest = UserTestFixture.defaultSignupRequest()
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
        SignupRequestDto request = UserTestFixture.createUniqueSignupRequest();

        // when
        SignupResponseDto response = authService.signup(request);

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
        SignupRequestDto request = UserTestFixture.defaultSignupRequest()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(UserTestFixture.generateUniqueNickname())
                .topics(topics)
                .build();

        // when
        SignupResponseDto response = authService.signup(request);

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

        SignupRequestDto request = SignupRequestDto.builder()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(UserTestFixture.generateUniqueNickname())
                .name("김테스트")
                .password("password123")
                .topics(tooManyTopics)
                .build();

        // when
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("토픽은 1개 이상 3개 이하로 선택해주세요");
        }
    }

    @Test
    void 정상적인_로그인_시_JWT_토큰을_반환한다() {
        // given
        SignupRequestDto signupRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(signupRequest);

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();

        // when
        LoginResponseDto response = authService.login(loginRequest);

        // then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600000L);
    }

    @Test
    void 존재하지_않는_이메일로_로그인_시_예외가_발생한다() {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .email("notfound@test.com")
                .password("password123")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 잘못된_비밀번호로_로그인_시_예외가_발생한다() {
        // given
        SignupRequestDto signupRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(signupRequest);

        LoginRequestDto wrongPasswordRequest = LoginRequestDto.builder()
                .email(signupRequest.email())
                .password("wrongpassword")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 정상적인_로그인_시_리프레시_토큰도_함께_반환한다() {
        // given
        SignupRequestDto signupRequest = UserTestFixture.createUniqueSignupRequest();
        authService.signup(signupRequest);

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();

        // when
        LoginResponseDto response = authService.login(loginRequest);

        // then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600000L);
    }
}