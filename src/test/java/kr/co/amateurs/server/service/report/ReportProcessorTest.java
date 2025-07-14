package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.LLMAnalysisResult;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.fixture.report.ReportTestFixtures;
import kr.co.amateurs.server.service.report.llm.ReportLLMService;
import kr.co.amateurs.server.service.report.processor.ReportProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportProcessorTest {

    @Mock
    private ReportLLMService reportLLMService;

    @InjectMocks
    private ReportProcessor reportProcessor;

    @Test
    void 게시글_신고_처리_성공_위반_없음() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();
        var post = ReportTestFixtures.createNormalPost(author);
        var report = ReportTestFixtures.createPostReport(user, post, "테스트 신고");

        var expectedResult = new LLMAnalysisResult(
                false,
                "위반 사항이 발견되지 않았습니다",
                0.9,
                true
        );

        when(reportLLMService.analyzeContent(anyString(), any(ReportType.class), anyString()))
                .thenReturn(expectedResult);

        // when
        LLMAnalysisResult result = reportProcessor.process(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isViolation()).isFalse();
        assertThat(result.success()).isTrue();
        assertThat(result.confidenceScore()).isEqualTo(0.9);
        assertThat(result.reason()).isEqualTo("위반 사항이 발견되지 않았습니다");

        verify(reportLLMService, times(1))
                .analyzeContent(anyString(), eq(ReportType.BAD_WORDS), anyString());
    }

    @Test
    void 게시글_신고_처리_성공_위반_확인() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();
        var post = ReportTestFixtures.createViolationPost(author);
        var report = ReportTestFixtures.createPostReport(user, post, "욕설이 포함된 게시글");

        var expectedResult = new LLMAnalysisResult(
                true,
                "부적절한 언어 사용이 감지되었습니다",
                0.85,
                true
        );

        when(reportLLMService.analyzeContent(anyString(), any(ReportType.class), anyString()))
                .thenReturn(expectedResult);

        // when
        LLMAnalysisResult result = reportProcessor.process(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isViolation()).isTrue();
        assertThat(result.success()).isTrue();
        assertThat(result.confidenceScore()).isEqualTo(0.85);
        assertThat(result.isHighConfidence()).isTrue();
        assertThat(result.reason()).isEqualTo("부적절한 언어 사용이 감지되었습니다");

        verify(reportLLMService, times(1))
                .analyzeContent(anyString(), eq(ReportType.BAD_WORDS), anyString());
    }

    @Test
    void 댓글_신고_처리_성공() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();
        var post = ReportTestFixtures.createNormalPost(author);
        var comment = ReportTestFixtures.createCustomComment(post, author, "부적절한 댓글 내용");
        var report = ReportTestFixtures.createCommentReport(user, comment, "욕설 댓글 신고");

        var expectedResult = new LLMAnalysisResult(
                true,
                "댓글에서 부적절한 표현이 발견되었습니다",
                0.75,
                true
        );

        when(reportLLMService.analyzeContent(anyString(), any(ReportType.class), anyString()))
                .thenReturn(expectedResult);

        // when
        LLMAnalysisResult result = reportProcessor.process(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isViolation()).isTrue();
        assertThat(result.success()).isTrue();
        assertThat(result.confidenceScore()).isEqualTo(0.75);
        assertThat(result.needsManualReview()).isTrue();
        assertThat(result.reason()).isEqualTo("댓글에서 부적절한 표현이 발견되었습니다");

        verify(reportLLMService, times(1))
                .analyzeContent(eq("부적절한 댓글 내용"), eq(ReportType.BAD_WORDS), eq("욕설 댓글 신고"));
    }

    @Test
    void 낮은_신뢰도_결과_처리() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();
        var post = ReportTestFixtures.createNormalPost(author);
        var report = ReportTestFixtures.createPostReport(user, post, "애매한 내용");

        var expectedResult = new LLMAnalysisResult(
                false,
                "명확한 위반 사항을 판단하기 어렵습니다",
                0.3,
                true
        );

        when(reportLLMService.analyzeContent(anyString(), any(ReportType.class), anyString()))
                .thenReturn(expectedResult);

        // when
        LLMAnalysisResult result = reportProcessor.process(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isViolation()).isFalse();
        assertThat(result.success()).isTrue();
        assertThat(result.confidenceScore()).isEqualTo(0.3);
        assertThat(result.isLowConfidence()).isTrue();
        assertThat(result.needsManualReview()).isFalse();

        verify(reportLLMService, times(1))
                .analyzeContent(anyString(), eq(ReportType.BAD_WORDS), anyString());
    }

    @Test
    void LLM_서비스_예외_발생_시_오류_처리() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();
        var post = ReportTestFixtures.createNormalPost(author);
        var report = ReportTestFixtures.createPostReport(user, post, "테스트 신고");

        when(reportLLMService.analyzeContent(anyString(), any(ReportType.class), anyString()))
                .thenThrow(new RuntimeException("LLM 서비스 오류"));

        // when
        LLMAnalysisResult result = reportProcessor.process(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isViolation()).isFalse();
        assertThat(result.success()).isFalse();
        assertThat(result.confidenceScore()).isEqualTo(0.0);
        assertThat(result.reason()).contains("처리 중 오류 발생");
        assertThat(result.reason()).contains("LLM 서비스 오류");

        verify(reportLLMService, times(1))
                .analyzeContent(anyString(), eq(ReportType.BAD_WORDS), anyString());
    }

    @Test
    void 게시글이_없는_신고_처리_빈_내용() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var report = Report.builder()
                .user(user)
                .post(null)
                .comment(null)
                .description("잘못된 신고")
                .reportType(ReportType.BAD_WORDS)
                .build();

        var expectedResult = new LLMAnalysisResult(
                false,
                "분석할 내용이 없습니다",
                0.0,
                true
        );

        when(reportLLMService.analyzeContent(eq(""), any(ReportType.class), anyString()))
                .thenReturn(expectedResult);

        // when
        LLMAnalysisResult result = reportProcessor.process(report);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isViolation()).isFalse();
        assertThat(result.success()).isTrue();
        assertThat(result.confidenceScore()).isEqualTo(0.0);

        verify(reportLLMService, times(1))
                .analyzeContent(eq(""), eq(ReportType.BAD_WORDS), eq("잘못된 신고"));
    }

    @Test
    void 게시글_내용_추출_테스트_제목_내용_태그_결합() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();
        var post = ReportTestFixtures.createCustomPost(
                author,
                "테스트 제목",
                "테스트 내용",
                kr.co.amateurs.server.domain.entity.post.enums.BoardType.FREE
        );
        var report = ReportTestFixtures.createPostReport(user, post, "테스트 신고");

        var expectedResult = new LLMAnalysisResult(false, "테스트", 0.5, true);
        when(reportLLMService.analyzeContent(anyString(), any(ReportType.class), anyString()))
                .thenReturn(expectedResult);

        // when
        reportProcessor.process(report);

        // then
        verify(reportLLMService, times(1))
                .analyzeContent(
                        eq("테스트 제목 테스트 내용 태그1,태그2"),
                        eq(ReportType.BAD_WORDS),
                        eq("테스트 신고")
                );
    }

    @Test
    void 다양한_신고_타입_처리_테스트() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();
        var post = ReportTestFixtures.createNormalPost(author);

        // 스팸 신고
        var spamReport = Report.builder()
                .user(user)
                .post(post)
                .description("스팸 게시글입니다")
                .reportType(ReportType.SPAM)
                .build();

        var expectedResult = new LLMAnalysisResult(
                true,
                "스팸으로 판단됩니다",
                0.9,
                true
        );

        when(reportLLMService.analyzeContent(anyString(), eq(ReportType.SPAM), anyString()))
                .thenReturn(expectedResult);

        // when
        LLMAnalysisResult result = reportProcessor.process(spamReport);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isViolation()).isTrue();
        assertThat(result.isHighConfidence()).isTrue();

        verify(reportLLMService, times(1))
                .analyzeContent(anyString(), eq(ReportType.SPAM), eq("스팸 게시글입니다"));
    }

    @Test
    void null_값_처리_테스트_안전한_문자열_추출() {
        // given
        var user = ReportTestFixtures.createTestUser();
        var author = ReportTestFixtures.createGuestUser();

        // null 값들이 포함된 게시글
        var post = kr.co.amateurs.server.domain.entity.post.Post.builder()
                .user(author)
                .title(null)  // null 제목
                .content(null)  // null 내용
                .tags(null)  // null 태그
                .boardType(kr.co.amateurs.server.domain.entity.post.enums.BoardType.FREE)
                .build();

        var report = ReportTestFixtures.createPostReport(user, post, "테스트 신고");

        var expectedResult = new LLMAnalysisResult(false, "내용 없음", 0.0, true);
        when(reportLLMService.analyzeContent(anyString(), any(ReportType.class), anyString()))
                .thenReturn(expectedResult);

        // when
        reportProcessor.process(report);

        // then - 빈 문자열들이 결합되어 공백으로 처리되어야 함
        verify(reportLLMService, times(1))
                .analyzeContent(
                        eq("  "),  // 빈 문자열들이 공백으로 결합됨
                        eq(ReportType.BAD_WORDS),
                        eq("테스트 신고")
                );
    }
}
