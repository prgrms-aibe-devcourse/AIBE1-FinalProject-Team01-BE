package kr.co.amateurs.server.controller.together;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.together.MarketJooqRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.restassured.RestAssured.given;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.*;
import static kr.co.amateurs.server.fixture.together.GatheringTestFixture.createGatheringRequestDTO;
import static kr.co.amateurs.server.fixture.together.MarketTestFixture.createMarketPostRequestDTO;
import static org.hamcrest.Matchers.*;

class MarketControllerTest extends AbstractControllerTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private MarketJooqRepository marketJooqRepository;

    private String guestEmail;
    private String adminEmail;
    private String studentEmail;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        marketRepository.deleteAll();
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
                    .get("/market")
                    .then()
                    .statusCode(200)
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(10));
        }

        @Test
        void 학생유저가_유효한데이터로_글생성하면_201과_응답검증한다() {
            MarketPostRequestDTO dto = createMarketPostRequestDTO();

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when()
                    .post("/market")
                    .then()
                    .statusCode(201)
                    .body("postId", notNullValue())
                    .body("title", equalTo(dto.title()));
        }

        @Test
        void 학생유저가_필수필드누락된데이터로_글생성하면_400을_반환한다() {
            MarketPostRequestDTO invalid = new MarketPostRequestDTO(
                    "", "내용", "Tag", MarketStatus.SELLING, 1000, "장소");

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(invalid)
                    .when()
                    .post("/market")
                    .then()
                    .statusCode(400);
        }

        @Test
        void 학생유저가_글생성후_ID조회하면_200과_본문을_검증한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMarketPostRequestDTO())
                    .when()
                    .post("/market")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long marketId = marketRepository.findByPostId(id).getId();

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .when()
                    .get("/market/{marketId}", marketId)
                    .then()
                    .statusCode(200)
                    .body("postId", equalTo(id.intValue()));
        }

        @Test
        void 학생유저가_존재하지않는ID로_조회하면_404를_반환한다() {
            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .when()
                    .get("/market/{postId}", 99999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 학생유저가_기존글생성후_수정하면_204를_반환한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMarketPostRequestDTO())
                    .when()
                    .post("/market")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long marketId = marketRepository.findByPostId(id).getId();

            MarketPostRequestDTO update = new MarketPostRequestDTO(
                    "수정", "수정내용", "Vue",
                    MarketStatus.SELLING, 10000, "서울"
            );

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(update)
                    .when()
                    .put("/market/{marketId}", marketId)
                    .then()
                    .statusCode(204);
        }

        @Test
        void 유저가_타인글수정하면_401을_반환한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMarketPostRequestDTO())
                    .when()
                    .post("/market")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");

            given()
                    .header("Authorization", "Bearer fake-other-user-token")
                    .contentType(ContentType.JSON)
                    .body(createMarketPostRequestDTO())
                    .when()
                    .put("/market/{postId}", id)
                    .then()
                    .statusCode(401);
        }

        @Test
        void 학생유저가_글생성후_삭제요청_204를_반환한다() {
            Long id = given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .contentType(ContentType.JSON)
                    .body(createMarketPostRequestDTO())
                    .when()
                    .post("/market")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long marketId = marketRepository.findByPostId(id).getId();

            given()
                    .header("Authorization", "Bearer " + fakeStudentToken())
                    .when()
                    .delete("/market/{marketId}", marketId)
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
                    .get("/market")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    class AdminTests {
        @Test
        void 관리자유저가_키워드를포함하여_페이지조회하면_200과_페이지를_검증한다() {
            int dataCount = marketJooqRepository.countByKeyword("React");
            given()
                    .header("Authorization", "Bearer " + fakeAdminToken())
                    .param("page", 0)
                    .param("size", 5)
                    .param("keyword", "React")
                    .when()
                    .get("/market")
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
                    .body(createMarketPostRequestDTO())
                    .when()
                    .post("/market")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("postId");
            Long marketId = marketRepository.findByPostId(id).getId();

            given()
                    .header("Authorization", "Bearer " + fakeAdminToken())
                    .when()
                    .delete("/market/{marketId}", marketId)
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
                    .get("/market")
                    .then()
                    .statusCode(401);
        }

        @Test
        void 인증없이_수정요청하면_401을_반환한다() {
            MarketPostRequestDTO update = createMarketPostRequestDTO();

            given()
                    .contentType(ContentType.JSON)
                    .body(update)
                    .when()
                    .put("/market/{marketId}", 1L)
                    .then()
                    .statusCode(401);
        }

        @Test
        void 인증없이_삭제요청하면_401을_반환한다() {
            given()
                    .when()
                    .delete("/market/{marketId}", 1L)
                    .then()
                    .statusCode(401);
        }
    }
}