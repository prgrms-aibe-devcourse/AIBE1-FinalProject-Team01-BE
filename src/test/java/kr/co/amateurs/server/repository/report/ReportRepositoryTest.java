package kr.co.amateurs.server.repository.report;

import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
public class ReportRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    private User testUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("테스트유저")
                .role(Role.STUDENT)
                .build();
        entityManager.persistAndFlush(testUser);

        testPost = Post.builder()
                .user(testUser)
                .title("테스트 제목")
                .content("테스트 내용")
                .boardType(BoardType.FREE)
                .build();
        entityManager.persistAndFlush(testPost);

        testComment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글")
                .build();
        entityManager.persistAndFlush(testComment);
    }

    @Test
    void 게시글_신고를_생성하면_정상적으로_저장되어야_한다() {
        // Given
        Report report = Report.fromPost(testPost, testUser, "부적절한 내용");

        // When
        Report savedReport = reportRepository.save(report);

        // Then
        assertThat(savedReport.getId()).isNotNull();
        assertThat(savedReport.getUser()).isEqualTo(testUser);
        assertThat(savedReport.getPost()).isEqualTo(testPost);
        assertThat(savedReport.getDescription()).isEqualTo("부적절한 내용");
        assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void 댓글_신고를_생성하면_정상적으로_저장되어야_한다() {
        // Given
        Report report = Report.fromComment(testComment, testUser, "욕설 포함");

        // When
        Report savedReport = reportRepository.save(report);

        // Then
        assertThat(savedReport.getId()).isNotNull();
        assertThat(savedReport.getUser()).isEqualTo(testUser);
        assertThat(savedReport.getComment()).isEqualTo(testComment);
        assertThat(savedReport.getDescription()).isEqualTo("욕설 포함");
        assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void 신고_상태를_업데이트하면_변경된_상태가_저장되어야_한다() {
        // Given
        Report report = Report.fromPost(testPost, testUser, "테스트 신고");
        Report savedReport = reportRepository.save(report);

        // When
        savedReport.updateStatusReport(ReportStatus.RESOLVED);
        reportRepository.save(savedReport);

        // Then
        Report updatedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        assertThat(updatedReport.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 특정_상태의_신고들만_조회하면_해당_상태의_신고만_반환되어야_한다() {
        // Given
        Report pendingReport = Report.fromPost(testPost, testUser, "대기 중인 신고");
        reportRepository.save(pendingReport);

        Report reviewedReport = Report.fromPost(testPost, testUser, "검토된 신고");
        reviewedReport.updateStatusReport(ReportStatus.REVIEWED);
        reportRepository.save(reviewedReport);

        Report reviewedReport2 = Report.fromPost(testPost, testUser, "검토된 신고2");
        reviewedReport2.updateStatusReport(ReportStatus.REVIEWED);
        reportRepository.save(reviewedReport2);

        Report rejectedReport = Report.fromPost(testPost, testUser, "거절된 신고");
        rejectedReport.updateStatusReport(ReportStatus.REJECTED);
        reportRepository.save(rejectedReport);

        // When
        List<Report> pendingReports = reportRepository.findByStatus(ReportStatus.PENDING);
        List<Report> reviewedReports = reportRepository.findByStatus(ReportStatus.REVIEWED);
        List<Report> rejectedReports = reportRepository.findByStatus(ReportStatus.REJECTED);

        // Then
        assertThat(pendingReports).hasSize(1);
        assertThat(pendingReports.get(0).getDescription()).isEqualTo("대기 중인 신고");
        assertThat(pendingReports.get(0).getStatus()).isEqualTo(ReportStatus.PENDING);

        assertThat(reviewedReports).hasSize(2);
        assertThat(reviewedReports.get(0).getDescription()).isEqualTo("검토된 신고");
        assertThat(reviewedReports.get(0).getStatus()).isEqualTo(ReportStatus.REVIEWED);

        assertThat(rejectedReports).hasSize(1);
        assertThat(rejectedReports.get(0).getDescription()).isEqualTo("거절된 신고");
        assertThat(rejectedReports.get(0).getStatus()).isEqualTo(ReportStatus.REJECTED);
    }
}
