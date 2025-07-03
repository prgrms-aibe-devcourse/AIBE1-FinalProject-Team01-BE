package kr.co.amateurs.server.service.report;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
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
            log.info("신고 분석 시작 - 타입: {}, 콘텐츠 길이: {}", reportType, content.length());

            String typeDescription = getTypeDescription(reportType);

            String response = reportAnalysis.analyzeReport(
                    content,
                    reportType.name(),
                    typeDescription,
                    description
            );

            LLMAnalysisResult result = parseGeminiResponse(response);

            log.info("신고 분석 완료 - 위반여부: {}, 신뢰도: {}",
                    result.isViolation(), result.confidenceScore());

            return result;

        } catch (Exception e) {
            log.error("Gemini 신고 분석 중 오류 발생: ", e);
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

    private LLMAnalysisResult parseGeminiResponse(String response) {
        try {
            // JSON 응답에서 코드 블록 제거
            String cleanedResponse = response.replaceAll("```json\\s*", "").replaceAll("```", "").trim();

            // 간단한 JSON 파싱 (실제로는 Jackson 등을 사용하는 것이 좋음)
            boolean isViolation = cleanedResponse.contains("\"isViolation\": true") ||
                    cleanedResponse.contains("\"isViolation\":true");

            String reason = extractJsonValue(cleanedResponse, "reason");
            double confidenceScore = parseConfidenceScore(cleanedResponse);

            return new LLMAnalysisResult(isViolation, reason, confidenceScore, true);

        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: ", e);
            return new LLMAnalysisResult(
                    false,
                    "응답 파싱 실패",
                    0.0,
                    false
            );
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            return "값을 추출할 수 없음";
        } catch (Exception e) {
            return "파싱 오류";
        }
    }

    private double parseConfidenceScore(String json) {
        try {
            String pattern = "\"confidenceScore\"\\s*:\\s*([0-9.]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                double score = Double.parseDouble(m.group(1));
                return Math.max(0.0, Math.min(1.0, score)); // 0.0 ~ 1.0 범위로 제한
            }
            return 0.5; // 기본값
        } catch (Exception e) {
            return 0.0;
        }
    }

}
