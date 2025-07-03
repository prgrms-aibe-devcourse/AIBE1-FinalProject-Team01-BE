package kr.co.amateurs.server.service.report;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ReportAnalysis {
    @UserMessage("""
        다음 텍스트가 '{{reportTypeDescription}}' 규정을 위반하는지 분석해주세요.
        
        신고 타입: {{reportType}} ({{reportTypeDescription}})
        신고자 설명: {{description}} (참고용, 판단은 주로 '분석할 내용'을 기준으로)
        분석할 내용: {{content}}
        
        다음 JSON 형식으로만 응답해주세요:
        {
            "isViolation": true/false,
            "reason": "판단 이유 (구체적으로 50자 이내 설명)",
            "confidenceScore": 0.0-1.0,
        }
        
        분석 기준:
        - 명백한 위반: confidenceScore 0.8 이상
        - 애매한 경우: confidenceScore 0.5 이상 0.8미만 (수동 검토 필요)
        - 와전한 안전: confidenceScore 0.5 미만
        - 한국어 맥락과 문화를 고려하여 판단
        - 단순 의견 표현과 악의적 공격 구분
        - 맥락을 고려한 정확한 판단
        """)
    String analyzeReport(
            @V("content") String content,
            @V("reportType") String reportType,
            @V("reportTypeDescription") String reportTypeDescription,
            @V("description") String description
    );
}
