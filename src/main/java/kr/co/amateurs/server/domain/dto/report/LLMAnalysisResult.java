package kr.co.amateurs.server.domain.dto.report;

public record LLMAnalysisResult(
        boolean isViolation,
        String reason,
        double confidenceScore,
        boolean success
) {

    public boolean isHighConfidence() {
        return confidenceScore >= 0.8;
    }

    public boolean isLowConfidence() {
        return confidenceScore < 0.5;
    }

    public boolean needsManualReview() {
        return confidenceScore >= 0.5 && confidenceScore < 0.8;
    }
}
