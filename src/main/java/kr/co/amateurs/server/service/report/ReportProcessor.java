package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.LLMAnalysisResult;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportTarget;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportProcessor {
    private final ReportLLMService reportLLMService;

    public LLMAnalysisResult process(Report report) {
        try {
            log.info("{} 신고 처리 시작 - Report ID: {}",
                    report.getReportType().getDescription(), report.getId());

            String content = extractContent(report);

            LLMAnalysisResult result = reportLLMService.analyzeContent(
                    content,
                    report.getReportType(),
                    report.getDescription()
            );

            log.info("{} 분석 완료 - Report ID: {}, 위반여부: {}, 신뢰도: {}",
                    report.getReportType().getDescription(), report.getId(),
                    result.isViolation(), result.confidenceScore());

            return result;

        } catch (Exception e) {
            log.error("{} 신고 처리 실패 - Report ID: {}",

                    report.getReportType().getDescription(), report.getId(), e);
            return new LLMAnalysisResult(
                    false,
                    "처리 중 오류 발생: " + e.getMessage(),
                    0.0,
                    false
            );
        }
    }

    private String extractContent(Report report) {
        if (report.getPost() != null) {
            // 게시글의 경우 제목 + 내용
            String title = report.getPost().getTitle() != null ? report.getPost().getTitle() : "";
            String content = report.getPost().getContent() != null ? report.getPost().getContent() : "";
            return title + " " + content;
        } else if (report.getComment() != null) {
            // 댓글의 경우 내용만
            return report.getComment().getContent() != null ? report.getComment().getContent() : "";
        } else {
            log.warn("신고 대상이 명확하지 않음 - Report ID: {}", report.getId());
            return "";
        }
    }
}