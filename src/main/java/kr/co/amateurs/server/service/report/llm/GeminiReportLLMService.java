package kr.co.amateurs.server.service.report.llm;

import kr.co.amateurs.server.domain.dto.report.ReportAIResponse;
import kr.co.amateurs.server.domain.dto.report.LLMAnalysisResult;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiReportLLMService implements ReportLLMService {
    private final ReportAnalysis reportAnalysis;

    @Override
    public LLMAnalysisResult analyzeContent(String content, ReportType reportType, String description) {
        try {
            String typeDescription = getTypeDescription(reportType);

            ReportAIResponse response = reportAnalysis.analyzeReport(
                    content,
                    reportType.name(),
                    typeDescription,
                    description
            );

            return new LLMAnalysisResult(response.isViolation(), response.reason(), response.confidenceScore(), true);

        } catch (Exception e) {
            return new LLMAnalysisResult(
                    false,
                    "LLM 분석 실패: " + e.getMessage(),
                    0.0,
                    false
            );
        }
    }

    private String getTypeDescription(ReportType reportType) {
        return switch (reportType) {
            case BAD_WORDS -> "욕설, 비방, 모욕적 표현";
            case SPAM -> "스팸, 광고, 홍보성 게시물";
            case SEXUAL_CONTENT -> "음란물, 선정적 내용";
            case PERSONAL_INFO -> "개인정보 무단 노출";
            case FLOODING -> "도배성 게시물";
            case OTHER -> "기타 커뮤니티 규정 위반";
        };
    }
}