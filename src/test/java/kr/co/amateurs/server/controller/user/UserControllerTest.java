package kr.co.amateurs.server.controller.user;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDto;
import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.fixture.auth.AuthTestFixture;
import kr.co.amateurs.server.fixture.auth.TokenTestFixture;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Import(EmbeddedRedisConfig.class)
public class UserControllerTest extends AbstractControllerTest {

    @Test
    void 로그인된_사용자의_프로필_조회가_성공한다() {
        // given
        SignupRequestDto signupRequest = UserTestFixture.createUniqueSignupRequest();
        given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        LoginRequestDto loginRequest = AuthTestFixture.defaultLoginRequest()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();

        String accessToken = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");

        // when & then
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("email", equalTo(signupRequest.email()))
                .body("nickname", equalTo(signupRequest.nickname()))
                .body("name", equalTo(signupRequest.name()))
                .body("topics", not(empty()));
    }

    @Test
    void 인증_토큰_없이_프로필_조회_시_404_에러가_발생한다() {
        // when & then
        given()
                .when()
                .get("/users/me")
                .then()
                .statusCode(404);
    }

    @Test
    void 잘못된_토큰으로_프로필_조회_시_404_에러가_발생한다() {
        // given
        String invalidToken = TokenTestFixture.INVALID_TOKEN;

        // when & then
        given()
                .header("Authorization", "Bearer " + invalidToken)
                .when()
                .get("/users/me")
                .then()
                .statusCode(404);
    }
}