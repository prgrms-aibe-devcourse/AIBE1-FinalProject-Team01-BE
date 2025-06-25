package kr.co.amateurs.server.fixture.auth;

import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDto;

public class AuthTestFixture {

    public static LoginRequestDto.LoginRequestDtoBuilder defaultLoginRequest() {
        return LoginRequestDto.builder()
                .email(UserTestFixture.DEFAULT_EMAIL)
                .password(UserTestFixture.DEFAULT_PASSWORD);
    }

    public static LoginRequestDto createValidLoginRequest() {
        return defaultLoginRequest().build();
    }

    public static LoginRequestDto createLoginRequestWithEmail(String email) {
        return defaultLoginRequest().email(email).build();
    }

    public static LoginRequestDto createInvalidEmailLoginRequest() {
        return defaultLoginRequest().email("invalid-email").build();
    }

    public static LoginRequestDto createEmptyEmailLoginRequest() {
        return defaultLoginRequest().email("").build();
    }

    public static LoginRequestDto createWrongPasswordLoginRequest() {
        return defaultLoginRequest().password("wrongpassword").build();
    }

    public static LoginRequestDto createEmptyPasswordLoginRequest() {
        return defaultLoginRequest().password("").build();
    }

    public static LoginRequestDto createNonExistentUserLoginRequest() {
        return defaultLoginRequest().email("nonexistent@test.com").build();
    }
}