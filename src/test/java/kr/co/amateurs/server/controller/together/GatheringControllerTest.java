package kr.co.amateurs.server.controller.together;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.fakeAdminToken;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.fakeStudentToken;
import static kr.co.amateurs.server.fixture.together.GatheringTestFixture.*;
import static org.hamcrest.Matchers.*;

class GatheringControllerTest extends AbstractControllerTest {


    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("학생 유저는 팀원 모집 글 목록 조회가 성공해야 한다")
    void 학생_유저는_팀원_모집_글_목록_조회가_성공해야_한다() {
        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .param("page", 0)
                .param("size", 10)
                .when()
                .get("/api/v1/gatherings")
                .then()
                .statusCode(200)
                .time(lessThan(2000L))
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("totalPages", greaterThanOrEqualTo(0))
                .body("currentPage", equalTo(0))
                .body("size", equalTo(10));
    }

    @Test
    @DisplayName("관리자 유저는 팀원 모집 글 목록 조회가 성공해야 한다")
    void 관리자_유저는_팀원_모집_글_목록_조회가_성공해야_한다() {
        given()
                .header("Authorization", "Bearer " + fakeAdminToken())
                .param("page", 0)
                .param("size", 5)
                .param("keyword", "React")
                .when()
                .get("/api/v1/gatherings")
                .then()
                .statusCode(200)
                .time(lessThan(2000L))
                .body("content", notNullValue())
                .body("size", equalTo(5));
    }

    @Test
    @DisplayName("인증되지 않은 유저는 팀원 모집 글 목록 조회가 실패해야 한다")
    void 인증되지_않은_유저는_팀원_모집_글_목록_조회가_실패해야_한다() {
        given()
                .param("page", 0)
                .param("size", 10)
                .when()
                .get("/api/v1/gatherings")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("학생 유저는 팀원 모집 글 생성이 성공해야 한다")
    void 학생_유저는_팀원_모집_글_생성이_성공해야_한다() {
        GatheringPostRequestDTO requestDTO = createValidRequestDTO();

        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(201)
                .time(lessThan(3000L))
                .body("postId", notNullValue())
                .body("title", equalTo("팀원 모집합니다"))
                .body("content", equalTo("React 프로젝트 함께할 팀원을 모집합니다."))
                .body("tags", hasItems("React", "JavaScript", "Frontend"))
                .body("gatheringType", equalTo("PROJECT"))
                .body("headCount", equalTo(4))
                .body("place", equalTo("온라인"))
                .body("period", equalTo("3개월"))
                .body("schedule", equalTo("주 2회 화/목 오후 7시"))
                .body("status", equalTo("RECRUITING"))
                .body("likeCount", equalTo(0))
                .body("viewCount", equalTo(0))
                .body("hasLiked", equalTo(false))
                .body("hasBookmarked", equalTo(false));
    }

    @Test
    @DisplayName("필수 필드가 누락된 팀원 모집 글 생성은 실패해야 한다")
    void 필수_필드가_누락된_팀원_모집_글_생성은_실패해야_한다() {
        GatheringPostRequestDTO invalidRequestDTO = new GatheringPostRequestDTO(
                "", // 빈 제목
                "내용",
                "Tag",
                GatheringType.SIDE_PROJECT,
                GatheringStatus.RECRUITING,
                4,
                "장소",
                "기간",
                "일정"
        );

        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(invalidRequestDTO)
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("학생 유저는 특정 팀원 모집 글 조회가 성공해야 한다")
    void 학생_유저는_특정_팀원_모집_글_조회가_성공해야_한다() {
        // 먼저 글을 생성하고 ID를 받아온다
        Long postId = given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(createValidRequestDTO())
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(201)
                .extract()
                .path("postId");

        // 생성된 글을 조회한다
        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .when()
                .get("/api/v1/gatherings/{postId}", postId)
                .then()
                .statusCode(200)
                .time(lessThan(2000L))
                .body("postId", equalTo(postId.intValue()))
                .body("title", equalTo("팀원 모집합니다"))
                .body("content", notNullValue())
                .body("authorNickname", notNullValue())
                .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("존재하지 않는 팀원 모집 글 조회는 실패해야 한다")
    void 존재하지_않는_팀원_모집_글_조회는_실패해야_한다() {
        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .when()
                .get("/api/v1/gatherings/{postId}", 99999L)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("작성자는 본인의 팀원 모집 글 수정이 성공해야 한다")
    void 작성자는_본인의_팀원_모집_글_수정이_성공해야_한다() {
        // 먼저 글을 생성한다
        Long postId = given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(createValidRequestDTO())
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(201)
                .extract()
                .path("postId");

        // 수정할 내용
        GatheringPostRequestDTO updateRequestDTO = new GatheringPostRequestDTO(
                "수정된 팀원 모집",
                "수정된 내용입니다.",
                "Vue",
                GatheringType.STUDY,
                GatheringStatus.RECRUITING,
                5,
                "오프라인",
                "2개월",
                "주 3회"
        );

        // 글을 수정한다
        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(updateRequestDTO)
                .when()
                .put("/api/v1/gatherings/{postId}", postId)
                .then()
                .statusCode(204)
                .time(lessThan(2000L));
    }

    @Test
    @DisplayName("작성자가 아닌 유저는 팀원 모집 글 수정이 실패해야 한다")
    void 작성자가_아닌_유저는_팀원_모집_글_수정이_실패해야_한다() {
        // 학생 유저가 글을 생성한다
        Long postId = given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(createValidRequestDTO())
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(201)
                .extract()
                .path("postId");

        // 다른 유저의 토큰으로 수정을 시도한다 (실제로는 다른 유저 토큰 생성 필요)
        GatheringPostRequestDTO updateRequestDTO = createValidRequestDTO();

        given()
                .header("Authorization", "Bearer fake-other-user-token")
                .contentType(ContentType.JSON)
                .body(updateRequestDTO)
                .when()
                .put("/api/v1/gatherings/{postId}", postId)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("작성자는 본인의 팀원 모집 글 삭제가 성공해야 한다")
    void 작성자는_본인의_팀원_모집_글_삭제가_성공해야_한다() {
        // 먼저 글을 생성한다
        Long postId = given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(createValidRequestDTO())
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(201)
                .extract()
                .path("postId");

        // 글을 삭제한다
        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .when()
                .delete("/api/v1/gatherings/{postId}", postId)
                .then()
                .statusCode(204)
                .time(lessThan(2000L));

        // 삭제된 글을 조회하면 404가 반환되어야 한다
        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .when()
                .get("/api/v1/gatherings/{postId}", postId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 팀원 모집 글 삭제가 성공해야 한다")
    void 관리자는_다른_사용자의_팀원_모집_글_삭제가_성공해야_한다() {
        // 학생 유저가 글을 생성한다
        Long postId = given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(createValidRequestDTO())
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(201)
                .extract()
                .path("postId");

        // 관리자가 글을 삭제한다
        given()
                .header("Authorization", "Bearer " + fakeAdminToken())
                .when()
                .delete("/api/v1/gatherings/{postId}", postId)
                .then()
                .statusCode(204)
                .time(lessThan(2000L));
    }

    @Test
    @DisplayName("작성자가 아닌 일반 유저는 팀원 모집 글 삭제가 실패해야 한다")
    void 작성자가_아닌_일반_유저는_팀원_모집_글_삭제가_실패해야_한다() {
        // 학생 유저가 글을 생성한다
        Long postId = given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .contentType(ContentType.JSON)
                .body(createValidRequestDTO())
                .when()
                .post("/api/v1/gatherings")
                .then()
                .statusCode(201)
                .extract()
                .path("postId");

        // 다른 유저가 글 삭제를 시도한다
        given()
                .header("Authorization", "Bearer fake-other-user-token")
                .when()
                .delete("/api/v1/gatherings/{postId}", postId)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("페이지네이션 파라미터가 유효하지 않으면 실패해야 한다")
    void 페이지네이션_파라미터가_유효하지_않으면_실패해야_한다() {
        given()
                .header("Authorization", "Bearer " + fakeStudentToken())
                .param("page", -1)
                .param("size", 0)
                .when()
                .get("/api/v1/gatherings")
                .then()
                .statusCode(400);
    }
}