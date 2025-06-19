package kr.co.amateurs.server.service.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.domain.dto.auth.SignupResponseDto;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.topic.UserTopicRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTopicRepository userTopicRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void 정상적인_유저_등록_시_201_응답을_반환한다() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password("password123")
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND))
                .build();

        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password(encodedPassword)
                .role(Role.GUEST)
                .providerType(ProviderType.LOCAL)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        SignupResponseDto response = authService.signup(request);

        // then
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.nickname()).isEqualTo("testnick");

        verify(userService).validateEmailDuplicate("test@test.com");
        verify(userService).validateNicknameDuplicate("testnick");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userTopicRepository).saveAll(any());
    }

    @Test
    void 중복된_이메일로_회원가입_시_예외가_발생한다() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("duplicate@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password("password123")
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND))
                .build();

        doThrow(ErrorCode.DUPLICATE_EMAIL.get())
                .when(userService).validateEmailDuplicate("duplicate@test.com");

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userService).validateEmailDuplicate("duplicate@test.com");
        verify(userService, never()).validateNicknameDuplicate(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(userTopicRepository, never()).saveAll(any());
    }

    @Test
    void 중복된_닉네임으로_회원가입_시_예외가_발생한다() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("test@test.com")
                .nickname("duplicateNick")
                .name("김테스트")
                .password("password123")
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND))
                .build();

        doThrow(ErrorCode.DUPLICATE_NICKNAME.get())
                .when(userService).validateNicknameDuplicate("duplicateNick");

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);

        verify(userService).validateEmailDuplicate("test@test.com");
        verify(userService).validateNicknameDuplicate("duplicateNick");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(userTopicRepository, never()).saveAll(any());
    }

    @Test
    void 회원가입_시_비밀번호가_실제로_BCrypt_암호화된다() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password("rawPassword123")
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND))
                .build();

        PasswordEncoder realEncoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(authService, "passwordEncoder", realEncoder);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);

            try {
                Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, 1L);

                Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(user, LocalDateTime.now());

                return user;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // when
        SignupResponseDto response = authService.signup(request);

        // then
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.nickname()).isEqualTo("testnick");
        assertThat(response.userId()).isEqualTo(1L);

        // 실제 암호화 검증!
        verify(userRepository).save(argThat(user -> {
            String actualEncryptedPassword = user.getPassword();

            System.out.println("원본 비밀번호: rawPassword123");
            System.out.println("암호화된 비밀번호: " + actualEncryptedPassword);

            return realEncoder.matches("rawPassword123", actualEncryptedPassword) &&
                    !actualEncryptedPassword.equals("rawPassword123") &&
                    actualEncryptedPassword.startsWith("$2a$") &&
                    actualEncryptedPassword.length() > 50 &&
                    user.getRole() == Role.GUEST &&
                    user.getProviderType() == ProviderType.LOCAL;
        }));

        verify(userTopicRepository).saveAll(any());
    }


    @Test
    void 토픽이_올바르게_UserTopic으로_변환되어_저장된다() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password("password123")
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND, Topic.AI))
                .build();

        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password(encodedPassword)
                .role(Role.GUEST)
                .providerType(ProviderType.LOCAL)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        SignupResponseDto response = authService.signup(request);

        // then
        assertThat(response.topics()).hasSize(3);
        assertThat(response.topics()).containsExactlyInAnyOrder(
                Topic.FRONTEND, Topic.BACKEND, Topic.AI
        );

        verify(userTopicRepository).saveAll(argThat(userTopics -> {
            List<UserTopic> userTopicList = (List<UserTopic>) userTopics;
            return userTopicList.size() == 3 &&
                    userTopicList.stream().anyMatch(ut -> ut.getTopic() == Topic.FRONTEND) &&
                    userTopicList.stream().anyMatch(ut -> ut.getTopic() == Topic.BACKEND) &&
                    userTopicList.stream().anyMatch(ut -> ut.getTopic() == Topic.AI) &&
                    userTopicList.stream().allMatch(ut -> ut.getUser().equals(savedUser));
        }));
    }

    @Test
    void 토픽_4개_이상_선택_시_validation_에러가_발생한다() {
        // given
        Set<Topic> tooManyTopics = Set.of(
                Topic.FRONTEND, Topic.BACKEND, Topic.AI, Topic.MOBILE
        );

        SignupRequestDto request = SignupRequestDto.builder()
                .email("test@test.com")
                .nickname("testnick")
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
            assertThat(violations.iterator().next().getPropertyPath().toString())
                    .isEqualTo("topics");
        }
    }

    @Test
    void UserTopic_저장_실패_시_예외가_발생한다() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password("password123")
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND))
                .build();

        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password(encodedPassword)
                .role(Role.GUEST)
                .providerType(ProviderType.LOCAL)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userTopicRepository.saveAll(any())).thenThrow(new RuntimeException("DB 저장 실패"));

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 실패");

        verify(userRepository).save(any(User.class));
        verify(userTopicRepository).saveAll(any());
    }

}