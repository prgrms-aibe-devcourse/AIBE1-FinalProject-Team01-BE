package kr.co.amateurs.server.service.verification;

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

    public VerificationDTO verifyStudent(MultipartFile image) {
        try {
            // 1. OCR로 텍스트 추출
            String extractedText = ocrService.extractText(image);

            // 2. 간단한 키워드 체크
            boolean isValid = checkKeywords(extractedText);

            // 3. 결과 반환
            return VerificationDTO.builder()
                    .isValid(isValid)
                    .extractedText(extractedText)
                    .message(isValid ? "✅ 데브코스 수강생 인증 성공!" : "❌ 데브코스 관련 키워드를 찾을 수 없습니다.")
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

    private boolean checkKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        // "프로그래머스" 또는 "데브코스" 포함되어 있으면 인증 성공
        boolean hasKeyword = lowerText.contains("프로그래머스") ||
                lowerText.contains("데브코스") ||
                lowerText.contains("programmers") ||
                lowerText.contains("devcourse");

        log.info("키워드 체크 결과: {} (텍스트: {})", hasKeyword, text);
        return hasKeyword;
    }
}
