package kr.co.amateurs.server.controller.together;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.together.MatchJooqRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.restassured.RestAssured.given;
import static kr.co.amateurs.server.domain.entity.post.Post.convertTagToList;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.*;
import static kr.co.amateurs.server.fixture.together.MarketTestFixture.createMarketPostRequestDTO;
import static kr.co.amateurs.server.fixture.together.MatchTestFixture.createMatchPostRequestDTO;
import static org.hamcrest.Matchers.*;

class MatchControllerTest extends AbstractControllerTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private MatchJooqRepository matchJooqRepository;

    private String guestEmail;
    private String adminEmail;
    private String studentEmail;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        matchRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        User admin = createAdmin();
        User student = createStudent();
        User guest = createGuest();
        List<User> saved = userRepository.saveAll(List.of(admin, student, guest));
        adminEmail   = saved.get(0).getEmail();
        studentEmail = saved.get(1).getEmail();
        guestEmail   = saved.get(2).getEmail();
    }

    private String fakeStudentToken() {
        return jwtProvider.generateAccessToken(studentEmail);
    }

    private String fakeGuestToken() {
        return jwtProvider.generateAccessToken(guestEmail);
    }

    private String fakeAdminToken() {
        return jwtProvider.generateAccessToken(adminEmail);
    }

    @Nested
    class StudentTests {

        @Test
        void 학생유저가_유효한페이지사이즈로_목록조회하면_200을_반환한다() {
            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .param("page", 0)
                    .param("size", 10)
                    .when()
                    .get("/matches")
                    .then()
                    .statusCode(200)
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(10));
        }

        @Test
        void 학생유저가_유효한데이터로_글생성하면_201과_응답검증한다() {
            MatchPostRequestDTO dto = createMatchPostRequestDTO();

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when()
                    .post("/matches")
                    .then()
                    .statusCode(201)
                    .body("postId", notNullValue())
                    .body("title", equalTo(dto.title()));
        }

        @Test
        void 학생유저가_필수필드누락된데이터로_글생성하면_400을_반환한다() {
            MatchPostRequestDTO invalid = new MatchPostRequestDTO(
                    "", "내용", convertTagToList("Tag"), MatchingType.COFFEE_CHAT,
                    MatchingStatus.OPEN, "분야"
            );

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(invalid)
                    .when()
                    .post("/matches")
                    .then()
                    .statusCode(400);
        }

        @Test
        void 학생유저가_글생성후_ID조회하면_200과_본문을_검증한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMatchPostRequestDTO())
                    .when()
                    .post("/matches")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long matchId = matchRepository.findByPostId(id).getId();

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .when()
                    .get("/matches/{matchId}", matchId)
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(id.intValue()));
        }

        @Test
        void 학생유저가_존재하지않는ID로_조회하면_404를_반환한다() {
            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .when()
                    .get("/matches/{postId}", 99999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 학생유저가_기존글생성후_수정하면_204를_반환한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMatchPostRequestDTO())
                    .when()
                    .post("/matches")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long matchId = matchRepository.findByPostId(id).getId();

            MatchPostRequestDTO update = new MatchPostRequestDTO(
                    "수정", "수정내용", convertTagToList("Vue"), MatchingType.COFFEE_CHAT,
                    MatchingStatus.OPEN, "프론트엔드"
            );

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(update)
                    .when()
                    .put("/matches/{matchId}", matchId)
                    .then()
                    .statusCode(204);
        }

        @Test
        void 유저가_타인글수정하면_401을_반환한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMatchPostRequestDTO())
                    .when()
                    .post("/matches")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long matchId = matchRepository.findByPostId(id).getId();

            given()
                    .header("Authorization", "Bearer fake-other-user-token")
                    .contentType(ContentType.JSON)
                    .body(createMatchPostRequestDTO())
                    .when()
                    .put("/matches/{matchId}", matchId)
                    .then()
                    .statusCode(401);
        }

        @Test
        void 학생유저가_글생성후_삭제요청_204를_반환한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMatchPostRequestDTO())
                    .when()
                    .post("/matches")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long matchId = matchRepository.findByPostId(id).getId();

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .when()
                    .delete("/matches/{matchId}", matchId)
                    .then()
                    .statusCode(204);
        }

        @Test
        void 학생유저가_잘못된페이지파라미터로_목록조회하면_400을_반환한다() {
            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .param("page", -1)
                    .param("size", 0)
                    .when()
                    .get("/matches")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    class AdminTests {
        @Test
        void 관리자유저가_키워드를포함하여_페이지조회하면_200과_페이지를_검증한다() {
            int dataCount = matchJooqRepository.countByKeyword("React");

            given()
                    .header("Authorization", "Bearer " + fakeAdminToken())
                    .param("page", 0)
                    .param("size", 5)
                    .param("keyword", "React")
                    .when()
                    .get("/matches")
                    .then()
                    .statusCode(200)
                    .body("pageInfo.pageSize", equalTo(5))
                    .body("pageInfo.totalElements", equalTo(dataCount));
        }

        @Test
        void 관리자유저가_타인글삭제요청하면_204를_반환한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMatchPostRequestDTO())
                    .when()
                    .post("/matches")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long matchId = matchRepository.findByPostId(id).getId();

            given()
                    .header("Authorization", "Bearer " + fakeAdminToken())
                    .when()
                    .delete("/matches/{matchId}", matchId)
                    .then()
                    .statusCode(204);
        }
    }

    @Nested
    class GuestTests {
        @Test
        void 인증없이_페이지조회하면_401을_반환한다() {
            given()
                    .param("page", 0)
                    .param("size", 10)
                    .when()
                    .get("/matches")
                    .then()
                    .statusCode(401);
        }
        @Test
        void 인증없이_수정요청하면_401을_반환한다() {
            MatchPostRequestDTO update = createMatchPostRequestDTO();

            given()
                    .contentType(ContentType.JSON)
                    .body(update)
                    .when()
                    .put("/matches/{matchId}", 1L)
                    .then()
                    .statusCode(401);
        }

        @Test
        void 인증없이_삭제요청하면_401을_반환한다() {
            given()
                    .when()
                    .delete("/matches/{matchId}", 1L)
                    .then()
                    .statusCode(401);
        }
    }
}