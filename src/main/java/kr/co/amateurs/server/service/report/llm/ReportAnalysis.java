package kr.co.amateurs.server.service.report.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ReportAnalysis {
    @SystemMessage("""
        당신은 사용자의 신고를 분석하는 심사 담당자입니다.

        다음 조건에 따라 입력 내용을 분석하고 JSON 형식으로만 응답하세요:

        - 신고 타입 설명: '{{reportTypeDescription}}'
        - 한국어 맥락과 문화적 기준을 고려하여 판단합니다.
        - 단순 의견 표현과 악의적 공격을 명확히 구분합니다.
        - 맥락을 충분히 반영하여 판단합니다.
        - 아래 기준에 따라 confidenceScore를 설정하세요:
            - 명백한 위반: 0.8 이상
            - 애매한 경우 (수동 검토 필요): 0.5 이상 0.8 미만
            - 위반 아님: 0.5 미만

        응답은 반드시 다음 JSON 형식으로만 작성하세요:

        {
            "isViolation": true/false,
            "reason": "판단 이유 (50자 이내 구체적으로 설명)",
            "confidenceScore": 0.0-1.0
        }
        """)
    @UserMessage("""
        신고 타입: {{reportType}} ({{reportTypeDescription}})
        신고자 설명: {{description}} (참고용, 판단은 주로 '분석할 내용'을 기준으로)
        분석할 내용: {{content}}
        """)
    String analyzeReport(
            @V("content") String content,
            @V("reportType") String reportType,
            @V("reportTypeDescription") String reportTypeDescription,
            @V("description") String description
    );
}
