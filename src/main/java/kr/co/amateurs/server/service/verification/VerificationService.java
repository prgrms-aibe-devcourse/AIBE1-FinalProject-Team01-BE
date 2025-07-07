package kr.co.amateurs.server.service.verification;

import kr.co.amateurs.server.domain.dto.verfication.SimilarityAnalysisDTO;
import kr.co.amateurs.server.domain.dto.verfication.VerificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationService {
    private final OcrService ocrService;
    private final ImgSimilarityService imgsimilarityService; // 추가 필요

    // 가중치 설정
    private static final double OCR_WEIGHT = 0.7;
    private static final double SIMILARITY_WEIGHT = 0.3;
    private static final double PASS_THRESHOLD = 0.65;

    public VerificationDTO verifyStudent(MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();

            String extractedText = ocrService.extractText(image);
            double ocrScore = calculateOcrScore(extractedText);

            SimilarityAnalysisDTO similarityAnalysis = imgsimilarityService.analyzeSimilarity(imageBytes);
            double similarityScore = similarityAnalysis.getSimilarityScore();

            // 3. 최종 점수 계산
            double finalScore = (ocrScore * OCR_WEIGHT) + (similarityScore * SIMILARITY_WEIGHT);
            boolean isValid = finalScore >= PASS_THRESHOLD && similarityAnalysis.isLayoutSimilar();

            // 4. 상세 메시지 생성
            String detailMessage = generateDetailMessage(ocrScore, similarityScore, finalScore, similarityAnalysis);

            return VerificationDTO.builder()
                    .isValid(isValid)
                    .extractedText(extractedText)
                    .message(isValid ? "✅ 데브코스 수강생 인증 성공!" : "❌ 인증 실패")
                    .detailMessage(detailMessage)
                    .build();

        } catch (Exception e) {
            log.error("인증 처리 실패: ", e);
            return VerificationDTO.builder()
                    .isValid(false)
                    .extractedText("")
                    .message("❌ 이미지 처리 중 오류 발생: " + e.getMessage())
                    .build();
        }
    }

    private double calculateOcrScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        String lowerText = text.toLowerCase().replaceAll("\\s+", "");;

        boolean hasKdt = lowerText.contains("kdt");
        boolean hasDevcourse = lowerText.contains("데브코스");

        double score = 0.0;
        if (hasKdt) {
            score = +0.7;
        }

        if (hasDevcourse) {
            score = +0.3;
        }


        log.info("OCR 점수: {} (KDT: {}, 데브코스: {}, 텍스트: {})",
                score, hasKdt, hasDevcourse, lowerText);
        return score;
    }

    private String generateDetailMessage(double ocrScore, double similarityScore, double finalScore, SimilarityAnalysisDTO analysis) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("📊 분석 결과:\n"));
        message.append(String.format("• OCR 점수: %.2f (키워드 매칭)\n", ocrScore));
        message.append(String.format("• 유사도 점수: %.2f (레이아웃 분석)\n", similarityScore));
        message.append(String.format("• 최고 유사도: %.2f\n", analysis.getMaxSimilarity()));
        message.append(String.format("• 최종 점수: %.2f\n", finalScore));

        if (!analysis.isLayoutSimilar()) {
            message.append("⚠️ 프로그래머스 정식 레이아웃과 유사도가 낮습니다.");
        }
        return message.toString();
    }
}
