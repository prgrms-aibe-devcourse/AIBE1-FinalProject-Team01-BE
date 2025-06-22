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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private Report testPostReport;
    private Report testCommentReport;

    private ReportRequestDTO testPostRequestDTO;
    private ReportRequestDTO testCommentRequestDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .nickname("testUser")
                .email("test@test.com")
                .build();

        testPost = Post.builder()
                .user(testUser)
                .title("테스트 제목")
                .content("테스트 내용")
                .tags("테스트태그")
                .boardType(BoardType.FREE)
                .viewCount(10)
                .likeCount(5)
                .build();

        testComment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글")
                .build();

        testPostReport = Report.builder()
                .user(testUser)
                .post(testPost)
                .description("신고 내용")
                .status(ReportStatus.PENDING)
                .build();

        testCommentReport = Report.builder()
                .user(testUser)
                .comment(testComment)
                .description("댓글 신고 내용")
                .status(ReportStatus.PENDING)
                .build();

        testPostRequestDTO = new ReportRequestDTO(
                1L,
                ReportType.POST,
                "신고 내용"
        );

        testCommentRequestDTO = new ReportRequestDTO(
                1L,
                ReportType.COMMENT,
                "댓글 신고 내용"
        );


    }

    @Test
    void 모든_신고목록을_조회하면_신고목록이_반환되어야_한다() {
        // given
        List<Report> reportList = List.of(testPostReport, testCommentReport);
        given(reportRepository.findAll()).willReturn(reportList);

        // when
        List<ReportResponseDTO> result = reportService.getAllReports();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).description()).isEqualTo("신고 내용");
        assertThat(result.get(0).username()).isEqualTo("testUser");

        verify(reportRepository, times(1)).findAll();
    }

    @Test
    void 상태별_신고목록을_조회하면_해당_상태의_신고목록이_반환되어야_한다() {
        // given
        ReportStatus status = ReportStatus.PENDING;
        List<Report> reportList = List.of(testPostReport, testCommentReport);
        given(reportRepository.findByStatus(status)).willReturn(reportList);

        // when
        List<ReportResponseDTO> result = reportService.getReportsByStatus(status);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).ReportType()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.get(1).ReportType()).isEqualTo(ReportStatus.PENDING);

        verify(reportRepository, times(1)).findByStatus(status);
    }

    @Test
    void 유효한_게시글_신고_요청으로_신고를_생성하면_신고가_생성되어야_한다() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
        given(reportRepository.save(any(Report.class))).willReturn(testPostReport);

        // when
        ReportResponseDTO result = reportService.createReport(testPostRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("신고 내용");
        assertThat(result.post()).isEqualTo(testPost);

        verify(postRepository, times(1)).findById(1L);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void 유효한_댓글_신고_요청으로_신고를_생성하면_신고가_생성되어야_한다() {
        // given
        given(commentRepository.findById(1L)).willReturn(Optional.of(testComment));
        given(reportRepository.save(any(Report.class))).willReturn(testCommentReport);

        // when
        ReportResponseDTO result = reportService.createReport(testCommentRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("댓글 신고 내용");
        assertThat(result.comment()).isEqualTo(testComment);

        verify(commentRepository, times(1)).findById(1L);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void 존재하지않는_게시글로_신고를_생성하면_예외가_발생해야_한다() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        ReportRequestDTO invalidRequestDTO = new ReportRequestDTO(
                999L,
                ReportType.POST,
                "신고 설명"
        );

        // when & then
        assertThatThrownBy(() -> reportService.createReport(invalidRequestDTO))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");

        verify(postRepository, times(1)).findById(999L);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void 존재하지않는_댓글로_신고를_생성하면_예외가_발생해야_한다() {
        // given
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        ReportRequestDTO invalidRequestDTO = new ReportRequestDTO(
                999L,
                ReportType.COMMENT,
                "댓글 신고 설명"
        );

        // when & then
        assertThatThrownBy(() -> reportService.createReport(invalidRequestDTO))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");;

        verify(commentRepository, times(1)).findById(999L);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void 유효한_reportId로_신고상태를_수정하면_상태가_변경되어야_한다() {
        // given
        Long reportId = 1L;
        ReportStatus newStatus = ReportStatus.REVIEWED;

        given(reportRepository.findById(reportId)).willReturn(Optional.of(testPostReport));

        // when
        reportService.updateStatusReport(reportId, newStatus);

        // then
        verify(reportRepository, times(1)).findById(reportId);
        assertThat(testPostReport.getStatus()).isEqualTo(newStatus);
    }

    @Test
    void 존재하지않는_reportId로_신고상태를_수정하면_예외가_발생해야_한다() {
        // given
        Long reportId = 999L;
        ReportStatus newStatus = ReportStatus.REVIEWED;

        given(reportRepository.findById(reportId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.updateStatusReport(reportId, newStatus))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");;

        verify(reportRepository, times(1)).findById(reportId);
    }

    @Test
    void 유효한_reportId로_신고를_삭제하면_신고가_삭제되어야_한다() {
        // given
        Long reportId = 1L;

        given(reportRepository.findById(reportId)).willReturn(Optional.of(testPostReport));

        // when
        reportService.deleteReport(reportId);

        // then
        verify(reportRepository, times(1)).findById(reportId);
        verify(reportRepository, times(1)).delete(testPostReport);
    }

    @Test
    void 존재하지않는_reportId로_신고를_삭제하면_예외가_발생해야_한다() {
        // given
        Long reportId = 999L;

        given(reportRepository.findById(reportId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.deleteReport(reportId))
                .isInstanceOf(CustomException.class)
                .hasMessage("조회할 대상을 찾을 수 없습니다.");;

        verify(reportRepository, times(1)).findById(reportId);
        verify(reportRepository, never()).delete(any(Report.class));
    }

    @Test
    void 전체_목록이_빈_목록이면_빈_리스트가_반환되어야_한다() {
        // given
        given(reportRepository.findAll()).willReturn(List.of());

        // when
        List<ReportResponseDTO> result = reportService.getAllReports();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(reportRepository, times(1)).findAll();
    }

    @Test
    void 특정_상태의_신고가_없으면_빈_리스트가_반환되어야_한다() {
        // given
        ReportStatus status = ReportStatus.RESOLVED;
        given(reportRepository.findByStatus(status)).willReturn(List.of());

        // when
        List<ReportResponseDTO> result = reportService.getReportsByStatus(status);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(reportRepository, times(1)).findByStatus(status);
    }
}
