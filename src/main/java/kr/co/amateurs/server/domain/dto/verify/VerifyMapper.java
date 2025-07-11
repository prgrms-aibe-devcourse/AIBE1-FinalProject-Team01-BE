package kr.co.amateurs.server.domain.dto.verify;

import kr.co.amateurs.server.domain.entity.verify.Verify;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.verify.VerifyStatus;

import java.time.LocalDateTime;

public record VerifyMapper(
        String imageUrl,
        Long userId
) {
    public static Verify createEntity(
            User user,
            PythonServiceResponseDTO.DataDTO data,
            VerifyStatus status,
            String imageUrl,
            LocalDateTime verifiedAt) {
        return Verify.builder()
                .user(user)
                .status(status)
                .ocrScore(data.ocrScore())
                .layoutScore(data.layoutScore())
                .totalScore(data.totalScore())
                .extractedText(data.extractedText())
                .detailMessage(data.detailMessage())
                .imageUrl(imageUrl)
                .verifiedAt(verifiedAt)
                .completedAt(status == VerifyStatus.COMPLETED ? verifiedAt : null)
                .build();
    }

    public static Verify updateEntity(
            Verify existingVerify,
            PythonServiceResponseDTO.DataDTO data,
            VerifyStatus status) {
        return existingVerify.toBuilder()
                .status(status)
                .ocrScore(data.ocrScore())
                .layoutScore(data.layoutScore())
                .totalScore(data.totalScore())
                .extractedText(data.extractedText())
                .detailMessage(data.detailMessage())
                .completedAt(LocalDateTime.now())
                .build();
    }

    public static Verify updateToFailed(Verify existingVerify, String errorMessage) {
        return existingVerify.toBuilder()
                .status(VerifyStatus.FAILED)
                .detailMessage("처리 중 오류 발생: " + errorMessage)
                .completedAt(LocalDateTime.now())
                .build();
    }

}