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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Mock
    private UserService userService;

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
    void 신고_목록을_타입_상태_없이_조회하면_모든_신고_목록이_반환되어야_한다() {
        // given
        List<Report> reportList = List.of(testPostReport, testCommentReport);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(reportList, pageable, reportList.size());

        given(reportRepository.findByStatusAndType(
                eq(null),
                eq(null),
                eq(pageable)
        )).willReturn(reportPage);

        // when
        Page<ReportResponseDTO> result = reportService.getReports(null,null, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).description()).isEqualTo("신고 내용");
        assertThat(result.getContent().get(0).username()).isEqualTo("testUser");


        verify(reportRepository, times(1)).findByStatusAndType(
                eq(null),
                eq(null),
                eq(pageable)
        );
    }

    @Test
    void 특정_상태의_신고목록을_조회하면_해당_상태의_신고만_반환되어야_한다() {
        // given
        List<Report> pendingReports = List.of(testPostReport);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(pendingReports, pageable, pendingReports.size());

        given(reportRepository.findByStatusAndType(
                eq(ReportStatus.PENDING),
                eq(null),
                eq(pageable)
        )).willReturn(reportPage);

        // when
        Page<ReportResponseDTO> result = reportService.getReports(null, ReportStatus.PENDING, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).reportStatus()).isEqualTo(ReportStatus.PENDING);

        verify(reportRepository, times(1)).findByStatusAndType(
                eq(ReportStatus.PENDING),
                eq(null),
                eq(pageable)
        );
    }

    @Test
    void 특정_타입의_신고목록을_조회하면_해당_타입의_신고만_반환되어야_한다() {
        // given
        List<Report> postReports = List.of(testPostReport, testCommentReport);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(postReports, pageable, postReports.size());

        given(reportRepository.findByStatusAndType(
                eq(null),
                eq(ReportType.POST.name()),
                eq(pageable)
        )).willReturn(reportPage);

        // when
        Page<ReportResponseDTO> result = reportService.getReports(ReportType.POST, null, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).post()).isEqualTo(testPost);

        verify(reportRepository, times(1)).findByStatusAndType(
                eq(null),
                eq(ReportType.POST.name()),
                eq(pageable)
        );
    }

    @Test
    void 상태와_타입_모두_지정하여_신고목록을_조회하면_조건에_맞는_신고만_반환되어야_한다() {
        // given
        List<Report> filteredReports = List.of(testPostReport);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(filteredReports, pageable, filteredReports.size());

        given(reportRepository.findByStatusAndType(
                eq(ReportStatus.PENDING),
                eq(ReportType.POST.name()),
                eq(pageable)
        )).willReturn(reportPage);

        // when
        Page<ReportResponseDTO> result = reportService.getReports(ReportType.POST, ReportStatus.PENDING, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).reportStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.getContent().get(0).post()).isEqualTo(testPost);

        verify(reportRepository, times(1)).findByStatusAndType(
                eq(ReportStatus.PENDING),
                eq(ReportType.POST.name()),
                eq(pageable)
        );
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
    void 조건에_맞는_신고가_없으면_빈_페이지가_반환되어야_한다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(reportRepository.findByStatusAndType(
                eq(ReportStatus.RESOLVED),
                eq(ReportType.COMMENT.name()),
                eq(pageable)
        )).willReturn(emptyPage);

        // when
        Page<ReportResponseDTO> result = reportService.getReports(ReportType.COMMENT, ReportStatus.RESOLVED, 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(reportRepository, times(1)).findByStatusAndType(
                eq(ReportStatus.RESOLVED),
                eq(ReportType.COMMENT.name()),
                eq(pageable)
        );
    }
}
