package kr.co.amateurs.server.controller.report;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.fixture.report.ReportTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;


public class ReportControllerTest extends AbstractControllerTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private User adminUser;
    private User studentUser;
    private User guestUser;
    private Post testPost;
    private String adminToken;
    private String studentToken;
    private String guestToken;

    @BeforeEach
    void setUp() {
        bookmarkRepository.deleteAll();
        reportRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(ReportTestFixtures.createAdminUser());
        studentUser = userRepository.save(ReportTestFixtures.createStudentUser());
        guestUser = userRepository.save(ReportTestFixtures.createGuestUser());
        testPost = postRepository.save(ReportTestFixtures.createTestPost(adminUser));

        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        guestToken = jwtProvider.generateAccessToken(guestUser.getEmail());
    }

    @Test
    void STUDENT_권한으로_신고를_생성하면_성공해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                testPost.getId(),
                "부적절한 게시글 내용입니다"
        );

        // when & then
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + studentToken)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .statusCode(201)
                .body("reporterName", notNullValue())
                .body("description", equalTo("부적절한 게시글 내용입니다"))
                .body("reportStatus", equalTo("PENDING"))
                .body("postTitle", equalTo("테스트 제목"));

        assert reportRepository.count() == 1;
    }

    @Test
    void GUEST_권한으로_신고를_생성하면_성공해야_한다() {
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                testPost.getId(),
                "부적절한 게시글 내용입니다"
        );

        // when & then
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + guestToken)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .body("reporterName", notNullValue())
                .body("description", equalTo("부적절한 게시글 내용입니다"))
                .body("reportStatus", equalTo("PENDING"))
                .body("postTitle", equalTo("테스트 제목"));

        assert reportRepository.count() == 1;
    }

    @Test
    void 권한_없이_신고를_생성하면_401에러가_발생해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                testPost.getId(),
                "부적절한 게시글 내용입니다"
        );

        // when & then
        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .statusCode(401);
    }

    @Test
    void ADMIN_권한으로_신고_목록을_조회하면_성공해야_한다() {
        // given
        Report report1 = reportRepository.save(
                ReportTestFixtures.createPostReport(studentUser, testPost, "부적절한 내용")
        );
        Report report2 = reportRepository.save(
                ReportTestFixtures.createReportWithStatus(studentUser, testPost, "스팸 게시글", ReportStatus.RESOLVED)
        );

        // when & then
        given()
                .param("reportType", "POST")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/reports")
                .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("content[0].reporterName", equalTo("student"))
                .body("content[0].description", equalTo("부적절한 내용"))
                .body("content[0].reportStatus", equalTo("PENDING"))
                .body("content[1].description", equalTo("스팸 게시글"))
                .body("content[1].reportStatus", equalTo("RESOLVED"));
    }

    @Test
    void STUDENT_권한으로_신고_목록을_조회하면_403에러가_발생해야_한다() {
        // when & then
        given()
                .param("reportType", "POST")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + studentToken)
                .when()
                .get("/reports")
                .then()
                .statusCode(403);
    }

    @Test
    void 권한_없이_신고_목록을_조회하면_401에러가_발생해야_한다() {
        // when & then
        given()
                .param("reportType", "POST")
                .param("page", "0")
                .param("size", "10")
                .when()
                .get("/reports")
                .then()
                .statusCode(401);
    }

    @Test
    void ADMIN_권한으로_신고_상태를_업데이트하면_성공해야_한다() {
        // given
        Report report = reportRepository.save(
                ReportTestFixtures.createPostReport(studentUser, testPost, "부적절한 내용")
        );

        // when & then
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .put("/reports/{reportId}/{status}", report.getId(), ReportStatus.RESOLVED)
                .then()
                .statusCode(204);


        Report updatedReport = reportRepository.findById(report.getId()).orElseThrow();
        assert updatedReport.getStatus() == ReportStatus.RESOLVED;
    }

    @Test
    void STUDENT_권한으로_신고_상태를_업데이트하면_403에러가_발생해야_한다() {
        // given
        Report report = reportRepository.save(
                ReportTestFixtures.createPostReport(studentUser, testPost, "부적절한 내용")
        );

        // when & then
        given()
                .header("Authorization", "Bearer " + studentToken)
                .when()
                .put("/reports/{reportId}/{status}", report.getId(), ReportStatus.RESOLVED)
                .then()
                .statusCode(403);
    }

    @Test
    void ADMIN_권한으로_신고를_삭제하면_성공해야_한다() {
        // given
        Report report = reportRepository.save(
                ReportTestFixtures.createPostReport(studentUser, testPost, "부적절한 내용")
        );
        Long reportId = report.getId();

        // when & then
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/reports/{reportId}", reportId)
                .then()
                .statusCode(204);

        assert reportRepository.findById(reportId).isEmpty();
    }

    @Test
    void STUDENT_권한으로_신고를_삭제하면_403에러가_발생해야_한다() {
        // given
        Report report = reportRepository.save(
                ReportTestFixtures.createPostReport(studentUser, testPost, "부적절한 내용")
        );

        // when & then
        given()
                .header("Authorization", "Bearer " + studentToken)
                .when()
                .delete("/reports/{reportId}", report.getId())
                .then()
                .statusCode(403);
    }

    @Test
    void 빈_내용으로_신고_생성_요청하면_400에러가_발생해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                testPost.getId(),
                ""
        );

        // when & then
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_게시글로_신고_생성_요청하면_404에러가_발생해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                999L,
                "신고 내용"
        );

        // when & then
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .statusCode(404);
    }
}