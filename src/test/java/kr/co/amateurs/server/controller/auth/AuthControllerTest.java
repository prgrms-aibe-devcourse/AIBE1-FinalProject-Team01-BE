package kr.co.amateurs.server.controller.auth;

import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.fixture.auth.AuthTestFixture;
import kr.co.amateurs.server.fixture.auth.TokenTestFixture;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.domain.dto.auth.LoginRequestDTO;
import kr.co.amateurs.server.domain.dto.auth.SignupRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Import(EmbeddedRedisConfig.class)
public class AuthControllerTest extends AbstractControllerTest {

    @Test
    void 정상적인_로그인_요청_시_JWT_토큰이_반환되어야_한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        LoginRequestDTO loginRequest = AuthTestFixture.defaultLoginRequest()
                .email(signupRequest.email())
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo(TokenTestFixture.TOKEN_TYPE))
                .body("expiresIn", equalTo(TokenTestFixture.ACCESS_TOKEN_EXPIRATION.intValue()));
    }

    @Test
    void 존재하지_않는_사용자로_로그인_시_404_에러가_발생해야_한다() {
        // given
        LoginRequestDTO request = AuthTestFixture.createNonExistentUserLoginRequest();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("존재하지 않는 사용자입니다."));
    }

    @Test
    void 잘못된_비밀번호로_로그인_시_400_에러가_발생해야_한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        LoginRequestDTO wrongPasswordRequest = AuthTestFixture.defaultLoginRequest()
                .email(signupRequest.email())
                .password(TokenTestFixture.getWrongPassword())
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(wrongPasswordRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("비밀번호가 일치하지 않습니다."));
    }

    @Test
    void 잘못된_이메일_형식으로_로그인_시_400_에러가_발생해야_한다() {
        // given
        LoginRequestDTO invalidEmailRequest = AuthTestFixture.createInvalidEmailLoginRequest();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidEmailRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("올바른 이메일 형식이 아닙니다"));
    }

    @Test
    void 빈_이메일로_로그인_시_400_에러가_발생해야_한다() {
        // given
        LoginRequestDTO emptyEmailRequest = AuthTestFixture.createEmptyEmailLoginRequest();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(emptyEmailRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("이메일은 필수입니다"));
    }

    @Test
    void 빈_비밀번호로_로그인_시_400_에러가_발생해야_한다() {
        // given
        LoginRequestDTO emptyPasswordRequest = AuthTestFixture.createEmptyPasswordLoginRequest();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(emptyPasswordRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("비밀번호는 필수입니다"));
    }

    @Test
    void 정상적인_회원가입_요청_시_성공해야_한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201)
                .body("userId", notNullValue())
                .body("email", equalTo(signupRequest.email()))
                .body("nickname", equalTo(signupRequest.nickname()))
                .body("name", equalTo(signupRequest.name()));
    }

    @Test
    void 이메일_중복_시_400_에러가_발생해야_한다() {
        // given
        SignupRequestDTO firstSignupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(firstSignupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        SignupRequestDTO duplicateEmailRequest = UserTestFixture.defaultSignupRequest()
                .email(firstSignupRequest.email())
                .nickname(UserTestFixture.generateUniqueNickname())
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(duplicateEmailRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(400)
                .body("message", equalTo("이미 사용 중인 이메일입니다."));
    }

    @Test
    void 닉네임_중복_시_400_에러가_발생해야_한다() {
        // given
        SignupRequestDTO firstSignupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(firstSignupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        SignupRequestDTO duplicateNicknameRequest = UserTestFixture.defaultSignupRequest()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(firstSignupRequest.nickname())
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(duplicateNicknameRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(400)
                .body("message", equalTo("이미 사용 중인 닉네임입니다."));
    }

    @Test
    void 잘못된_이메일_형식으로_회원가입_시_400_에러가_발생해야_한다() {
        // given
        SignupRequestDTO invalidEmailRequest = UserTestFixture.defaultSignupRequest()
                .email("invalid-email")
                .nickname(UserTestFixture.generateUniqueNickname())
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidEmailRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(400)
                .body("message", equalTo("올바른 이메일 형식이 아닙니다"));
    }

    @Test
    void 빈_이메일로_회원가입_시_400_에러가_발생해야_한다() {
        // given
        SignupRequestDTO emptyEmailRequest = UserTestFixture.defaultSignupRequest()
                .email("")
                .nickname(UserTestFixture.generateUniqueNickname())
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(emptyEmailRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(400)
                .body("message", equalTo("이메일은 필수입니다"));
    }

    @Test
    void 짧은_비밀번호로_회원가입_시_400_에러가_발생해야_한다() {
        // given
        SignupRequestDTO shortPasswordRequest = UserTestFixture.defaultSignupRequest()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(UserTestFixture.generateUniqueNickname())
                .password("1234567")
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(shortPasswordRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(400)
                .body("message", equalTo("비밀번호는 최소 8자 이상이어야 합니다"));
    }

    @Test
    void 정상적인_로그인_시_리프레시_토큰도_함께_반환되어야_한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        LoginRequestDTO loginRequest = AuthTestFixture.defaultLoginRequest()
                .email(signupRequest.email())
                .build();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo(TokenTestFixture.TOKEN_TYPE))
                .body("expiresIn", equalTo(TokenTestFixture.ACCESS_TOKEN_EXPIRATION.intValue()));
    }

    @Test
    void 사용_가능한_이메일_확인_시_true를_반환한다() {
        // given
        String availableEmail = UserTestFixture.generateUniqueEmail();

        // when & then
        RestAssured.given()
                .param("email", availableEmail)
                .when()
                .get("/auth/check/email")
                .then()
                .statusCode(200)
                .body("available", equalTo(true))
                .body("message", equalTo("사용 가능한 이메일입니다"));
    }

    @Test
    void 이미_사용중인_이메일_확인_시_false를_반환한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        // when & then
        RestAssured.given()
                .param("email", signupRequest.email())
                .when()
                .get("/auth/check/email")
                .then()
                .statusCode(200)
                .body("available", equalTo(false))
                .body("message", equalTo("이미 사용중인 이메일입니다"));
    }

    @Test
    void 사용_가능한_닉네임_확인_시_true를_반환한다() {
        // given
        String availableNickname = UserTestFixture.generateUniqueNickname();

        // when & then
        RestAssured.given()
                .param("nickname", availableNickname)
                .when()
                .get("/auth/check/nickname")
                .then()
                .statusCode(200)
                .body("available", equalTo(true))
                .body("message", equalTo("사용 가능한 닉네임입니다"));
    }

    @Test
    void 이미_사용중인_닉네임_확인_시_false를_반환한다() {
        // given
        SignupRequestDTO signupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        // when & then
        RestAssured.given()
                .param("nickname", signupRequest.nickname())
                .when()
                .get("/auth/check/nickname")
                .then()
                .statusCode(200)
                .body("available", equalTo(false))
                .body("message", equalTo("이미 사용중인 닉네임입니다"));

    }

    @Test
    void 빈_이메일로_중복_확인_시_400_에러가_발생해야_한다() {
        // given
        String emptyEmail = "";

        // when & then
        RestAssured.given()
                .param("email", emptyEmail)
                .when()
                .get("/auth/check/email")
                .then()
                .statusCode(400)
                .body("message", equalTo("이메일은 필수입니다."));
    }

    @Test
    void 잘못된_이메일_형식으로_중복_확인_시_400_에러가_발생해야_한다() {
        // given
        String invalidEmail = "invalid-email";

        // when & then
        RestAssured.given()
                .param("email", invalidEmail)
                .when()
                .get("/auth/check/email")
                .then()
                .statusCode(400)
                .body("message", equalTo("올바른 이메일 형식이 아닙니다."));
    }

    @Test
    void 빈_닉네임으로_중복_확인_시_400_에러가_발생해야_한다() {
        // given
        String emptyNickname = "";

        // when & then
        RestAssured.given()
                .param("nickname", emptyNickname)
                .when()
                .get("/auth/check/nickname")
                .then()
                .statusCode(400)
                .body("message", equalTo("닉네임은 필수입니다."));
    }

    @Test
    void 긴_닉네임으로_중복_확인_시_400_에러가_발생해야_한다() {
        // given
        String longNickname = "a".repeat(21);

        // when & then
        RestAssured.given()
                .param("nickname", longNickname)
                .when()
                .get("/auth/check/nickname")
                .then()
                .statusCode(400)
                .body("message", equalTo("닉네임은 2자 이상 20자 이하여야 합니다."));
    }

    @Test
    void 유효한_리프레시_토큰으로_요청_시_새로운_액세스_토큰이_반환되어야_한다() {
        // given
        SignupRequestDto signupRequest = UserTestFixture.createUniqueSignupRequest();
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        LoginRequestDto loginRequest = AuthTestFixture.defaultLoginRequest()
                .email(signupRequest.email())
                .build();

        String refreshToken = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("refreshToken");

        Map<String, String> reissueRequest = Map.of("refreshToken", refreshToken);

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reissueRequest)
                .when()
                .post("/auth/reissue")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", equalTo(TokenTestFixture.ACCESS_TOKEN_EXPIRATION.intValue()));
    }

    @Test
    void 유효하지_않은_리프레시_토큰으로_재발급_요청_시_401_에러가_발생해야_한다() {
        // given
        Map<String, String> invalidRequest = Map.of("refreshToken", "invalid.token.here");

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/auth/reissue")
                .then()
                .statusCode(401)
                .body("message", equalTo("유효하지 않은 인증 정보입니다."));
    }

    @Test
    void 빈_리프레시_토큰으로_재발급_요청_시_400_에러가_발생해야_한다() {
        // given
        Map<String, String> emptyRequest = Map.of("refreshToken", "");

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(emptyRequest)
                .when()
                .post("/auth/reissue")
                .then()
                .statusCode(400)
                .body("message", equalTo("리프레시 토큰은 필수입니다"));
    }
}
