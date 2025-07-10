package kr.co.amateurs.server.domain.dto.verify;

public record PythonServiceResponseDTO(
    boolean success,
    String error,
    DataDTO data
) {
    public static record DataDTO(
        boolean isValid,
        String extractedText,
        String message,
        String detailMessage,
        int ocrScore,
        int layoutScore,
        int totalScore
    ) {}
} 