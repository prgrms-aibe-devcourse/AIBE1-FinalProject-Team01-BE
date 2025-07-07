package kr.co.amateurs.server.fixture.auth;

import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDTO;

public class AuthTestFixture {

    public static LoginRequestDTO.LoginRequestDTOBuilder defaultLoginRequest() {
        return LoginRequestDTO.builder()
                .email(UserTestFixture.DEFAULT_EMAIL)
                .password(UserTestFixture.DEFAULT_PASSWORD);
    }

    public static LoginRequestDTO createValidLoginRequest() {
        return defaultLoginRequest().build();
    }

    public static LoginRequestDTO createLoginRequestWithEmail(String email) {
        return defaultLoginRequest().email(email).build();
    }

    public static LoginRequestDTO createInvalidEmailLoginRequest() {
        return defaultLoginRequest().email("invalid-email").build();
    }

    public static LoginRequestDTO createEmptyEmailLoginRequest() {
        return defaultLoginRequest().email("").build();
    }

    public static LoginRequestDTO createWrongPasswordLoginRequest() {
        return defaultLoginRequest().password("wrongpassword").build();
    }

    public static LoginRequestDTO createEmptyPasswordLoginRequest() {
        return defaultLoginRequest().password("").build();
    }

    public static LoginRequestDTO createNonExistentUserLoginRequest() {
        return defaultLoginRequest().email("nonexistent@test.com").build();
    }
}