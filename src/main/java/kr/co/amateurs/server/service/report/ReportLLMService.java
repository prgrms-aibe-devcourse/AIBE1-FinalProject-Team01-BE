package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.LLMAnalysisResult;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;

public interface ReportLLMService {
    /**
     * 텍스트 콘텐츠를 분석하여 위반 여부를 판단
     *
     * @param content 분석할 텍스트 콘텐츠
     * @param reportType 신고 타입
     * @param description 신고자의 설명
     * @return LLM 분석 결과
     */
    LLMAnalysisResult analyzeContent(String content, ReportType reportType, String description);
}
