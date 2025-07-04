package kr.co.amateurs.server.controller.report;


import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.report.QueueStatus;
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
import kr.co.amateurs.server.service.report.processor.ReportProcessingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "ai.processing.enabled=true"
})
public class AIReportControllerTest extends AbstractControllerTest {
    @Autowired
    private ReportProcessingManager processingManager;

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

    private User studentUser;
    private User adminUser;
    private Post normalPost;
    private Post violationPost;
    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        bookmarkRepository.deleteAll();
        reportRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        studentUser = userRepository.save(ReportTestFixtures.createStudentUser());
        adminUser = userRepository.save(ReportTestFixtures.createAdminUser());

        normalPost = postRepository.save(ReportTestFixtures.createNormalPost(adminUser));
        violationPost = postRepository.save(ReportTestFixtures.createViolationPost(adminUser));

        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
    }

    @Test
    void 정상_게시글_신고_시_AI가_정상_판정해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(),
                "이 게시글이 부적절해 보입니다"
        );

        // when
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + studentToken)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .statusCode(201);

        Integer reportId = response.extract().path("id");

        waitForAIProcessing(reportId.longValue());

        Report finalReport = reportRepository.findById(reportId.longValue()).orElseThrow();
        assertThat(finalReport.getStatus()).isIn(
                ReportStatus.MANUAL_REVIEW,
                ReportStatus.REJECTED
        );
        assertThat(finalReport.getProcessingCompletedAt()).isNotNull();
        assertThat(finalReport.getIsViolation()).isFalse();
    }

    @Test
    void 위반_게시글_신고_시_AI가_위반_판정해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                violationPost.getId(),
                "욕설과 혐오 표현이 포함된 게시글입니다"
        );

        // when
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + studentToken)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .statusCode(201);

        Integer reportId = response.extract().path("id");

        // then
        waitForAIProcessing(reportId.longValue());

        Report finalReport = reportRepository.findById(reportId.longValue()).orElseThrow();
        assertThat(finalReport.getStatus()).isIn(
                ReportStatus.RESOLVED,
                ReportStatus.MANUAL_REVIEW
        );
        assertThat(finalReport.getProcessingCompletedAt()).isNotNull();

        if (finalReport.getConfidenceScore() != null && finalReport.getConfidenceScore() > 0.8) {
            assertThat(finalReport.getIsViolation()).isTrue();
        }
    }

    @Test
    void 신고_생성_후_AI_처리_큐에_자동_추가되어야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(),
                "AI 큐 테스트용 신고"
        );

        QueueStatus beforeStatus = processingManager.getQueueStatus();
        assertThat(beforeStatus.queueSize()).isEqualTo(0);

        // when
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + studentToken)
                .body(requestDTO)
                .when()
                .post("/reports")
                .then()
                .statusCode(201);

        QueueStatus afterStatus = processingManager.getQueueStatus();
        assertThat(afterStatus.isRunning()).isTrue();
        assertThat(afterStatus.isThreadAlive()).isTrue();
        assertThat(afterStatus.queueSize()).isEqualTo(0);

        Awaitility.await()
                .atMost(Duration.ofMinutes(2))
                .pollInterval(Duration.ofSeconds(3))
                .until(() -> processingManager.getQueueStatus().queueSize() == 0);
    }

    @Test
    void 여러_신고가_순차적으로_AI_처리되어야_한다() {
        // given
        ReportRequestDTO normalRequest = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(), "정상 게시글 신고"
        );
        ReportRequestDTO violationRequest1 = ReportTestFixtures.createPostReportRequestDTO(
                violationPost.getId(), "욕설이 포함된 게시글 신고"
        );
        ReportRequestDTO violationRequest2 = ReportTestFixtures.createPostReportRequestDTO(
                violationPost.getId(), "혐오 표현 포함 게시글 신고"
        );

        // when
        Integer reportId1 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + studentToken)
                .body(normalRequest)
                .when()
                .post("/reports")
                .then()
                .statusCode(201)
                .extract().path("id");

        Integer reportId2 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + studentToken)
                .body(violationRequest1)
                .when()
                .post("/reports")
                .then()
                .statusCode(201)
                .extract().path("id");

        Integer reportId3 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(violationRequest2)
                .when()
                .post("/reports")
                .then()
                .statusCode(201)
                .extract().path("id");

        // then
        waitForAIProcessing(reportId1.longValue());
        waitForAIProcessing(reportId2.longValue());
        waitForAIProcessing(reportId3.longValue());

        Report report1 = reportRepository.findById(reportId1.longValue()).orElseThrow();
        Report report2 = reportRepository.findById(reportId2.longValue()).orElseThrow();
        Report report3 = reportRepository.findById(reportId3.longValue()).orElseThrow();

        assertThat(report1.getStatus()).isNotEqualTo(ReportStatus.PENDING);
        assertThat(report2.getStatus()).isNotEqualTo(ReportStatus.PENDING);
        assertThat(report3.getStatus()).isNotEqualTo(ReportStatus.PENDING);

        assertThat(report1.getProcessingCompletedAt()).isNotNull();
        assertThat(report2.getProcessingCompletedAt()).isNotNull();
        assertThat(report3.getProcessingCompletedAt()).isNotNull();
    }

    private void waitForAIProcessing(Long reportId) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(3))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> isAIProcessingCompleted(reportId));
    }

    private boolean isAIProcessingCompleted(Long reportId) {
        return reportRepository.findById(reportId)
                .map(report -> !report.getStatus().equals(ReportStatus.PENDING) &&
                        !report.getStatus().equals(ReportStatus.PROCESSING) &&
                        report.getProcessingCompletedAt() != null)
                .orElse(false);
    }
}
