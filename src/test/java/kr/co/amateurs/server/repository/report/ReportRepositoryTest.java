package kr.co.amateurs.server.repository.report;

import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    void 모든_조건없이_조회하면_전체_신고가_반환되어야_한다() {
        // Given
        Report postReport = Report.fromPost(testPost, testUser, "게시글 신고");
        reportRepository.save(postReport);

        Report commentReport = Report.fromComment(testComment, testUser, "댓글 신고");
        reportRepository.save(commentReport);

        Report reviewedReport = Report.fromPost(testPost, testUser, "검토된 신고");
        reviewedReport.updateStatusReport(ReportStatus.REVIEWED);
        reportRepository.save(reviewedReport);

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Report> allReports = reportRepository.findByStatusAndType(null, null, pageable);

        // Then
        assertThat(allReports.getContent()).hasSize(3);
        assertThat(allReports.getTotalElements()).isEqualTo(3);
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

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Report> pendingReports = reportRepository.findByStatusAndType(ReportStatus.PENDING, null, pageable);
        Page<Report> reviewedReports = reportRepository.findByStatusAndType(ReportStatus.REVIEWED, null, pageable);
        Page<Report> rejectedReports = reportRepository.findByStatusAndType(ReportStatus.REJECTED, null, pageable);

        // Then
        assertThat(pendingReports.getContent()).hasSize(1);
        assertThat(pendingReports.getContent().get(0).getDescription()).isEqualTo("대기 중인 신고");
        assertThat(pendingReports.getContent().get(0).getStatus()).isEqualTo(ReportStatus.PENDING);

        assertThat(reviewedReports.getContent()).hasSize(2);
        assertThat(reviewedReports.getContent().get(0).getStatus()).isEqualTo(ReportStatus.REVIEWED);

        assertThat(rejectedReports.getContent()).hasSize(1);
        assertThat(rejectedReports.getContent().get(0).getDescription()).isEqualTo("거절된 신고");
        assertThat(rejectedReports.getContent().get(0).getStatus()).isEqualTo(ReportStatus.REJECTED);
    }

    @Test
    void 특정_타입의_신고들만_조회하면_해당_타입의_신고만_반환되어야_한다() {
        // Given
        Report postReport1 = Report.fromPost(testPost, testUser, "게시글 신고1");
        reportRepository.save(postReport1);

        Report postReport2 = Report.fromPost(testPost, testUser, "게시글 신고2");
        reportRepository.save(postReport2);

        Report commentReport = Report.fromComment(testComment, testUser, "댓글 신고");
        reportRepository.save(commentReport);

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Report> postReports = reportRepository.findByStatusAndType(null, ReportType.POST.name(), pageable);
        Page<Report> commentReports = reportRepository.findByStatusAndType(null, ReportType.COMMENT.name(), pageable);

        // Then
        assertThat(postReports.getContent()).hasSize(2);
        assertThat(postReports.getContent()).allMatch(report -> report.getPost() != null);
        assertThat(postReports.getContent()).allMatch(report -> report.getComment() == null);

        assertThat(commentReports.getContent()).hasSize(1);
        assertThat(commentReports.getContent().get(0).getComment()).isNotNull();
        assertThat(commentReports.getContent().get(0).getPost()).isNull();
        assertThat(commentReports.getContent().get(0).getDescription()).isEqualTo("댓글 신고");
    }

    @Test
    void 상태와_타입을_모두_지정하여_조회하면_조건에_맞는_신고만_반환되어야_한다() {
        // Given
        Report pendingPostReport = Report.fromPost(testPost, testUser, "대기 중인 게시글 신고");
        reportRepository.save(pendingPostReport);

        Report reviewedPostReport = Report.fromPost(testPost, testUser, "검토된 게시글 신고");
        reviewedPostReport.updateStatusReport(ReportStatus.REVIEWED);
        reportRepository.save(reviewedPostReport);

        Report pendingCommentReport = Report.fromComment(testComment, testUser, "대기 중인 댓글 신고");
        reportRepository.save(pendingCommentReport);

        Report reviewedCommentReport = Report.fromComment(testComment, testUser, "검토된 댓글 신고");
        reviewedCommentReport.updateStatusReport(ReportStatus.REVIEWED);
        reportRepository.save(reviewedCommentReport);

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Report> pendingPostReports = reportRepository.findByStatusAndType(ReportStatus.PENDING, ReportType.POST.name(), pageable);
        Page<Report> reviewedCommentReports = reportRepository.findByStatusAndType(ReportStatus.REVIEWED, ReportType.COMMENT.name(), pageable);

        // Then
        assertThat(pendingPostReports.getContent()).hasSize(1);
        assertThat(pendingPostReports.getContent().get(0).getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(pendingPostReports.getContent().get(0).getPost()).isNotNull();
        assertThat(pendingPostReports.getContent().get(0).getDescription()).isEqualTo("대기 중인 게시글 신고");

        assertThat(reviewedCommentReports.getContent()).hasSize(1);
        assertThat(reviewedCommentReports.getContent().get(0).getStatus()).isEqualTo(ReportStatus.REVIEWED);
        assertThat(reviewedCommentReports.getContent().get(0).getComment()).isNotNull();
        assertThat(reviewedCommentReports.getContent().get(0).getDescription()).isEqualTo("검토된 댓글 신고");
    }

    @Test
    void 조건에_맞는_신고가_없으면_빈_페이지가_반환되어야_한다() {
        // Given
        Report postReport = Report.fromPost(testPost, testUser, "게시글 신고");
        reportRepository.save(postReport);

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Report> emptyResult = reportRepository.findByStatusAndType(ReportStatus.RESOLVED, ReportType.COMMENT.name(), pageable);

        // Then
        assertThat(emptyResult.getContent()).isEmpty();
        assertThat(emptyResult.getTotalElements()).isEqualTo(0);
    }

    @Test
    void 페이지네이션이_정상적으로_동작해야_한다() {
        // Given
        for (int i = 1; i <= 5; i++) {
            Report report = Report.fromPost(testPost, testUser, "신고 " + i);
            reportRepository.save(report);
        }

        entityManager.flush();
        entityManager.clear();

        // When
        Pageable firstPage = PageRequest.of(0, 2);
        Page<Report> firstPageResult = reportRepository.findByStatusAndType(null, null, firstPage);

        // When
        Pageable secondPage = PageRequest.of(1, 2);
        Page<Report> secondPageResult = reportRepository.findByStatusAndType(null, null, secondPage);

        // Then
        assertThat(firstPageResult.getContent()).hasSize(2);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(5);
        assertThat(firstPageResult.getTotalPages()).isEqualTo(3);
        assertThat(firstPageResult.isFirst()).isTrue();
        assertThat(firstPageResult.hasNext()).isTrue();

        assertThat(secondPageResult.getContent()).hasSize(2);
        assertThat(secondPageResult.getTotalElements()).isEqualTo(5);
        assertThat(secondPageResult.getNumber()).isEqualTo(1);
        assertThat(secondPageResult.hasNext()).isTrue();
        assertThat(secondPageResult.hasPrevious()).isTrue();
    }

}
