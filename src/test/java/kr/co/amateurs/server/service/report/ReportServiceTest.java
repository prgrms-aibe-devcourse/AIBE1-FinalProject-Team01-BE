package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.dto.report.ReportResponseDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private User reporterUser;
    private User adminUser;
    private Post testPost;
    private Post testPost2;
    private Comment testComment;
    private Comment testComment2;
    private Report testPostReport;
    private Report testCommentReport;
    private Report reviewedReport;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(ReportTestFixtures.createTestUser());
        reporterUser = userRepository.save(ReportTestFixtures.createStudentUser());
        adminUser = userRepository.save(ReportTestFixtures.createAdminUser());

        testPost = postRepository.save(ReportTestFixtures.createTestPost(testUser));
        testPost2 = postRepository.save(ReportTestFixtures.createCustomPost(testUser, "다른 게시글", "다른 내용", BoardType.GATHER));

        testComment = commentRepository.save(ReportTestFixtures.createTestComment(testPost, testUser));
        testComment2 = commentRepository.save(ReportTestFixtures.createCustomComment(testPost2, testUser, "다른 댓글"));

        testPostReport = reportRepository.save(ReportTestFixtures.createPostReport(reporterUser, testPost, "부적절한 게시글"));
        testCommentReport = reportRepository.save(ReportTestFixtures.createCommentReport(reporterUser, testComment, "부적절한 댓글"));
        reviewedReport = reportRepository.save(ReportTestFixtures.createReportWithStatus(reporterUser, testPost2, "검토 완료된 신고", ReportStatus.REVIEWED));
    }

    @Test
    void 신고_목록을_타입_상태_없이_조회하면_모든_신고_목록이_반환되어야_한다() {
        // when
        Page<ReportResponseDTO> result = reportService.getReports(null, null, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3); // testPostReport, testCommentReport, reviewedReport
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void 특정_상태의_신고목록을_조회하면_해당_상태의_신고만_반환되어야_한다() {
        // when
        Page<ReportResponseDTO> result = reportService.getReports(null, ReportStatus.PENDING, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(report -> report.reportStatus() == ReportStatus.PENDING);
    }

    @Test
    void 특정_타입의_신고목록을_조회하면_해당_타입의_신고만_반환되어야_한다() {
        // when
        Page<ReportResponseDTO> result = reportService.getReports(ReportType.POST, null, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(report -> report.postTitle() != null);
    }

    @Test
    void 상태와_타입_모두_지정하여_신고목록을_조회하면_조건에_맞는_신고만_반환되어야_한다() {
        // when
        Page<ReportResponseDTO> result = reportService.getReports(ReportType.POST, ReportStatus.PENDING, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).reportStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.getContent().get(0).postTitle()).isNotNull();
        assertThat(result.getContent().get(0).description()).isEqualTo("부적절한 게시글");
    }

    @Test
    void 유효한_게시글_신고_요청으로_신고를_생성하면_신고가_생성되어야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(testPost2.getId(), "스팸 게시글");
        given(userService.getCurrentUser()).willReturn(Optional.of(reporterUser));

        // when
        ReportResponseDTO result = reportService.createReport(requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("스팸 게시글");
        assertThat(result.postTitle()).isNotNull();
        assertThat(result.postId()).isEqualTo(testPost2.getId());
        assertThat(result.reportStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.reporterName()).isEqualTo("student");
    }

    @Test
    void 유효한_댓글_신고_요청으로_신고를_생성하면_신고가_생성되어야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createCommentReportRequestDTO(testComment2.getId(), "욕설 댓글");
        given(userService.getCurrentUser()).willReturn(Optional.of(reporterUser));

        // when
        ReportResponseDTO result = reportService.createReport(requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("욕설 댓글");
        assertThat(result.commentContent()).isNotNull();
        assertThat(result.commentId()).isEqualTo(testComment2.getId());
        assertThat(result.reportStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.reporterName()).isEqualTo("student");
    }

    @Test
    void 존재하지않는_게시글로_신고를_생성하면_예외가_발생해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(999L, "존재하지 않는 게시글 신고");
        given(userService.getCurrentUser()).willReturn(Optional.of(reporterUser));

        // when & then
        assertThatThrownBy(() -> reportService.createReport(requestDTO))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");
    }

    @Test
    void 존재하지않는_댓글로_신고를_생성하면_예외가_발생해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createCommentReportRequestDTO(999L, "존재하지 않는 댓글 신고");
        given(userService.getCurrentUser()).willReturn(Optional.of(reporterUser));

        // when & then
        assertThatThrownBy(() -> reportService.createReport(requestDTO))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");
    }

    @Test
    void 로그인하지_않은_상태로_신고를_생성하면_예외가_발생해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(testPost.getId(), "로그인 없는 신고");
        given(userService.getCurrentUser()).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.createReport(requestDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유효한_reportId로_신고상태를_수정하면_상태가_변경되어야_한다() {
        // given
        Long reportId = testPostReport.getId();
        ReportStatus newStatus = ReportStatus.REVIEWED;

        // when
        reportService.updateStatusReport(reportId, newStatus);

        // then
        Report updatedReport = reportRepository.findById(reportId).orElseThrow();
        assertThat(updatedReport.getStatus()).isEqualTo(newStatus);
    }

    @Test
    void 존재하지않는_reportId로_신고상태를_수정하면_예외가_발생해야_한다() {
        // given
        Long nonExistentReportId = 999L;
        ReportStatus newStatus = ReportStatus.REVIEWED;

        // when & then
        assertThatThrownBy(() -> reportService.updateStatusReport(nonExistentReportId, newStatus))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");
    }

    @Test
    void 유효한_reportId로_신고를_삭제하면_신고가_삭제되어야_한다() {
        // given
        Long reportId = testCommentReport.getId();

        // when
        reportService.deleteReport(reportId);

        // then
        Optional<Report> deletedReport = reportRepository.findById(reportId);
        assertThat(deletedReport).isEmpty();
    }

    @Test
    void 존재하지않는_reportId로_신고를_삭제하면_예외가_발생해야_한다() {
        // given
        Long nonExistentReportId = 999L;

        // when & then
        assertThatThrownBy(() -> reportService.deleteReport(nonExistentReportId))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");
    }

    @Test
    void 조건에_맞는_신고가_없으면_빈_페이지가_반환되어야_한다() {
        // when
        Page<ReportResponseDTO> result = reportService.getReports(ReportType.COMMENT, ReportStatus.RESOLVED, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void 페이징이_정상적으로_동작해야_한다() {
        // given - 추가 신고 데이터 생성
        for (int i = 0; i < 15; i++) {
            Post additionalPost = postRepository.save(ReportTestFixtures.createCustomPost(testUser, "추가 게시글" + i, "내용" + i, BoardType.FREE));
            reportRepository.save(ReportTestFixtures.createPostReport(reporterUser, additionalPost, "추가 신고" + i));
        }

        // when
        Page<ReportResponseDTO> firstPage = reportService.getReports(null, null, 0, 10);
        Page<ReportResponseDTO> secondPage = reportService.getReports(null, null, 1, 10);

        // then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(secondPage.getContent()).hasSize(8);
        assertThat(firstPage.getTotalElements()).isEqualTo(18);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(secondPage.isLast()).isTrue();
    }
}
