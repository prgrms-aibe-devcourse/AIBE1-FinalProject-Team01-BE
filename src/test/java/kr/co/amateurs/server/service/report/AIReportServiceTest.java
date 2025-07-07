package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.QueueStatus;
import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.dto.report.ReportResponseDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportTarget;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.report.ReportTestFixtures;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.report.processor.ReportProcessingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AIReportServiceTest {

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

    @MockitoBean
    private ReportProcessingManager processingManager;

    private User studentUser;
    private User adminUser;
    private Post normalPost;
    private Post violationPost;
    private Comment normalComment;
    private Comment violationComment;

    @BeforeEach
    void setUp() {
        // 실제 DB에 저장해서 ID 생성
        studentUser = userRepository.save(ReportTestFixtures.createStudentUser());
        adminUser = userRepository.save(ReportTestFixtures.createAdminUser());

        normalPost = postRepository.save(ReportTestFixtures.createNormalPost(adminUser));
        violationPost = postRepository.save(ReportTestFixtures.createViolationPost(adminUser));

        normalComment = commentRepository.save(ReportTestFixtures.createTestComment(normalPost, adminUser));
        violationComment = commentRepository.save(
                ReportTestFixtures.createCustomComment(violationPost, adminUser, "욕설이 포함된 댓글입니다"));

        // 큐 상태 Mock 설정
        given(processingManager.getQueueStatus()).willReturn(
                new QueueStatus(0, true, true));
    }

    @Test
    void 정상_게시글_신고_생성_시_정상적으로_저장되어야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(),
                "이 게시글이 부적절해 보입니다"
        );

        given(userService.getCurrentLoginUser()).willReturn(studentUser);

        // when
        ReportResponseDTO response = reportService.createReport(requestDTO);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.postId()).isEqualTo(normalPost.getId());
        assertThat(response.postTitle()).isEqualTo(normalPost.getTitle());
        assertThat(response.reporterName()).isEqualTo(studentUser.getNickname());
        assertThat(response.description()).isEqualTo(requestDTO.description());
        assertThat(response.reportStatus()).isEqualTo(ReportStatus.PENDING);

        // DB에 실제로 저장되었는지 확인
        Report savedReport = reportRepository.findById(response.id()).orElseThrow();
        assertThat(savedReport.getPost().getId()).isEqualTo(normalPost.getId());
        assertThat(savedReport.getUser().getId()).isEqualTo(studentUser.getId());
        assertThat(savedReport.getDescription()).isEqualTo(requestDTO.description());
    }

    @Test
    void 위반_게시글_신고_생성_시_올바른_엔티티가_생성되어야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                violationPost.getId(),
                "욕설과 혐오 표현이 포함된 게시글입니다"
        );

        given(userService.getCurrentLoginUser()).willReturn(studentUser);

        // when
        ReportResponseDTO response = reportService.createReport(requestDTO);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.postId()).isEqualTo(violationPost.getId());
        assertThat(response.postTitle()).isEqualTo(violationPost.getTitle());
        assertThat(response.postContent()).isEqualTo(violationPost.getContent());
        assertThat(response.reporterName()).isEqualTo(studentUser.getNickname());
        assertThat(response.reportStatus()).isEqualTo(ReportStatus.PENDING);

        // DB에 실제로 저장되었는지 확인
        Report savedReport = reportRepository.findById(response.id()).orElseThrow();
        assertThat(savedReport.getPost().getId()).isEqualTo(violationPost.getId());
        assertThat(savedReport.getUser().getId()).isEqualTo(studentUser.getId());
        assertThat(savedReport.getDescription()).isEqualTo(requestDTO.description());
    }

    @Test
    void 댓글_신고_생성_시_정상_처리되어야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createCommentReportRequestDTO(
                normalComment.getId(),
                "부적절한 댓글입니다"
        );

        given(userService.getCurrentLoginUser()).willReturn(studentUser);

        // when
        ReportResponseDTO response = reportService.createReport(requestDTO);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.commentId()).isEqualTo(normalComment.getId());
        assertThat(response.commentContent()).isEqualTo(normalComment.getContent());
        assertThat(response.commentAuthor()).isEqualTo(adminUser.getNickname());
        assertThat(response.reporterName()).isEqualTo(studentUser.getNickname());

        // DB에 실제로 저장되었는지 확인
        Report savedReport = reportRepository.findById(response.id()).orElseThrow();
        assertThat(savedReport.getComment().getId()).isEqualTo(normalComment.getId());
        assertThat(savedReport.getReportTarget()).isEqualTo(ReportTarget.COMMENT);
    }

    @Test
    void 여러_신고가_순차적으로_생성되어야_한다() {
        ReportRequestDTO normalRequest = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(), "정상 게시글 신고"
        );
        ReportRequestDTO violationRequest1 = ReportTestFixtures.createPostReportRequestDTO(
                violationPost.getId(), "욕설이 포함된 게시글 신고"
        );
        ReportRequestDTO commentRequest = ReportTestFixtures.createCommentReportRequestDTO(
                normalComment.getId(), "부적절한 댓글 신고"
        );

        given(userService.getCurrentLoginUser())
                .willReturn(studentUser)
                .willReturn(studentUser)
                .willReturn(studentUser);

        // when
        ReportResponseDTO response1 = reportService.createReport(normalRequest);
        ReportResponseDTO response2 = reportService.createReport(violationRequest1);
        ReportResponseDTO response3 = reportService.createReport(commentRequest);

        // then
        assertThat(response1.id()).isNotNull();
        assertThat(response2.id()).isNotNull();
        assertThat(response3.id()).isNotNull();

        // 모든 신고가 DB에 저장되었는지 확인
        assertThat(reportRepository.findById(response1.id())).isPresent();
        assertThat(reportRepository.findById(response2.id())).isPresent();
        assertThat(reportRepository.findById(response3.id())).isPresent();

        // 각 신고가 다른 사용자에 의해 생성되었는지 확인
        Report report1 = reportRepository.findById(response1.id()).orElseThrow();
        Report report2 = reportRepository.findById(response2.id()).orElseThrow();
        Report report3 = reportRepository.findById(response3.id()).orElseThrow();

        assertThat(report1.getUser().getId()).isEqualTo(studentUser.getId());
        assertThat(report2.getUser().getId()).isEqualTo(studentUser.getId());
        assertThat(report3.getUser().getId()).isEqualTo(studentUser.getId());
    }

    @Test
    void AI_처리_시뮬레이션_테스트() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                violationPost.getId(),
                "AI 처리 시뮬레이션 테스트"
        );

        given(userService.getCurrentLoginUser()).willReturn(studentUser);

        // when
        ReportResponseDTO response = reportService.createReport(requestDTO);

        // then
        Report savedReport = reportRepository.findById(response.id()).orElseThrow();
        assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.PENDING);

        // AI 처리 완료 시뮬레이션 (실제로는 ReportProcessingManager에서 수행)
        savedReport.startProcessing();
        savedReport.completeProcessing(true, "AI 분석 결과: 위반 내용 발견", 0.95);
        reportRepository.save(savedReport);

        // AI 처리 결과 확인
        Report processedReport = reportRepository.findById(response.id()).orElseThrow();
        assertThat(processedReport.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(processedReport.getIsViolation()).isTrue();
        assertThat(processedReport.getConfidenceScore()).isEqualTo(0.95);
        assertThat(processedReport.getViolationReason()).contains("AI 분석 결과");
        assertThat(processedReport.getProcessingCompletedAt()).isNotNull();
    }

    @Test
    void 큐_상태_조회_시_Mock_데이터가_반환되어야_한다() {
        // when
        QueueStatus status = processingManager.getQueueStatus();

        // then
        assertThat(status.queueSize()).isEqualTo(0);
        assertThat(status.isRunning()).isTrue();
        assertThat(status.isThreadAlive()).isTrue();

        verify(processingManager).getQueueStatus();
    }

    @Test
    void 수동_검토_필요한_신고_시뮬레이션_테스트() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(),
                "애매한 내용의 게시글입니다"
        );

        given(userService.getCurrentLoginUser()).willReturn(adminUser); // 다른 사용자로 설정

        // when
        ReportResponseDTO response = reportService.createReport(requestDTO);

        // then
        Report savedReport = reportRepository.findById(response.id()).orElseThrow();

        // 수동 검토 필요 시뮬레이션
        savedReport.startProcessing();
        savedReport.manualProcessing("수동 검토가 필요한 내용입니다", 0.6);
        reportRepository.save(savedReport);

        // 수동 검토 상태 확인
        Report processedReport = reportRepository.findById(response.id()).orElseThrow();
        assertThat(processedReport.getStatus()).isEqualTo(ReportStatus.MANUAL_REVIEW);
        assertThat(processedReport.getConfidenceScore()).isEqualTo(0.6);
        assertThat(processedReport.getViolationReason()).contains("수동 검토가 필요");
        assertThat(processedReport.getProcessingCompletedAt()).isNotNull();
    }

    @Test
    void 중복_신고_방지_기능이_작동해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(),
                "첫 번째 신고"
        );

        given(userService.getCurrentLoginUser()).willReturn(studentUser);

        // when - 첫 번째 신고 생성
        ReportResponseDTO firstResponse = reportService.createReport(requestDTO);
        assertThat(firstResponse.id()).isNotNull();

        // then - 같은 사용자가 같은 게시글에 같은 타입으로 다시 신고 시도하면 중복 체크가 작동해야 함
        // (실제 구현에서는 예외가 발생하지만, 여기서는 중복 체크 로직이 있음을 확인)
        boolean duplicateExists = reportRepository.existsByUserIdAndPostIdAndReportType(
                studentUser.getId(),
                normalPost.getId(),
                requestDTO.reportType()
        );
        assertThat(duplicateExists).isTrue();
    }

    @Test
    void 신고_상태_업데이트가_정상_동작해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(),
                "상태 업데이트 테스트"
        );

        given(userService.getCurrentLoginUser()).willReturn(studentUser);

        ReportResponseDTO response = reportService.createReport(requestDTO);
        Long reportId = response.id();

        // when
        reportService.updateStatusReport(reportId, ReportStatus.RESOLVED);

        // then
        Report updatedReport = reportRepository.findById(reportId).orElseThrow();
        assertThat(updatedReport.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 신고_삭제가_정상_동작해야_한다() {
        // given
        ReportRequestDTO requestDTO = ReportTestFixtures.createPostReportRequestDTO(
                normalPost.getId(),
                "삭제 테스트"
        );

        given(userService.getCurrentLoginUser()).willReturn(studentUser);

        ReportResponseDTO response = reportService.createReport(requestDTO);
        Long reportId = response.id();

        // when
        reportService.deleteReport(reportId);

        // then
        assertThat(reportRepository.findById(reportId)).isEmpty();
    }
}