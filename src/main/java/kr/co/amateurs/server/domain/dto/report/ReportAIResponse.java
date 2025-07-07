package kr.co.amateurs.server.domain.dto.report;

public record ReportAIResponse(
    boolean isViolation,
    String reason,
    Double confidenceScore
){}
