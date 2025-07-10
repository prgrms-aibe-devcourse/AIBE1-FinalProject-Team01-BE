package kr.co.amateurs.server.domain.dto.verify;

import kr.co.amateurs.server.domain.entity.verify.Verify;
import kr.co.amateurs.server.domain.entity.verify.VerifyStatus;

public record VerifyResultDTO(
        int ocrScore,
        int layoutScore,
        int totalScore,
        String extractedText,
        String detailMessage,
        VerifyStatus status,
        boolean isVerified
) {
    public static VerifyResultDTO from(Verify verify) {
        return new VerifyResultDTO(
                verify.getOcrScore(),
                verify.getLayoutScore(),
                verify.getTotalScore(),
                verify.getExtractedText(),
                verify.getDetailMessage(),
                verify.getStatus(),
                verify.getStatus() == VerifyStatus.COMPLETED
        );
    }
}