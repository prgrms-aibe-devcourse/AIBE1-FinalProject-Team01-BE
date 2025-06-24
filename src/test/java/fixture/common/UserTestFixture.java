package fixture.common;

import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;

import java.util.Set;

public class UserTestFixture {

    public static final String DEFAULT_EMAIL = "test@test.com";
    public static final String DEFAULT_PASSWORD = "password123";
    public static final String DEFAULT_NICKNAME = "testnick";
    public static final String DEFAULT_NAME = "김테스트";

    public static User.UserBuilder defaultUser() {
        return User.builder()
                .email(DEFAULT_EMAIL)
                .nickname(DEFAULT_NICKNAME)
                .name(DEFAULT_NAME)
                .password("encodedPassword123")
                .role(Role.GUEST)
                .providerType(ProviderType.LOCAL);
    }

    public static User.UserBuilder guestUser() {
        return defaultUser()
                .role(Role.GUEST);
    }

    public static User createUser() {
        return defaultUser().build();
    }

    public static User createUserWithEmail(String email) {
        return defaultUser().email(email).build();
    }

    public static SignupRequestDto.SignupRequestDtoBuilder defaultSignupRequest() {
        return SignupRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .nickname(DEFAULT_NICKNAME)
                .name(DEFAULT_NAME)
                .password(DEFAULT_PASSWORD)
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND));
    }

    public static SignupRequestDto createSignupRequest() {
        return defaultSignupRequest().build();
    }

    public static SignupRequestDto createSignupRequestWithEmail(String email) {
        return defaultSignupRequest().email(email).build();
    }

    public static SignupRequestDto createInvalidEmailSignupRequest() {
        return defaultSignupRequest().email("invalid-email").build();
    }

    /**
     * 너무 많은 토픽을 가진 회원가입 요청 DTO를 생성합니다
     */
    public static SignupRequestDto createTooManyTopicsSignupRequest() {
        return defaultSignupRequest()
                .topics(Set.of(Topic.FRONTEND, Topic.BACKEND, Topic.AI, Topic.MOBILE))
                .build();
    }
}