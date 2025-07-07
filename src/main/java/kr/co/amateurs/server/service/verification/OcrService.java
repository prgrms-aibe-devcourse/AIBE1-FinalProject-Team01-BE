package kr.co.amateurs.server.service.verification;

import kr.co.amateurs.server.config.TesseractConfig;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        this.tesseract = new Tesseract();

        try {
            // 리소스를 임시 폴더로 복사
            File tempTessDataDir = copyTessDataToTemp();
            tesseract.setDatapath(tempTessDataDir.getAbsolutePath());
            tesseract.setLanguage("kor+eng");
            log.info("Tesseract OCR 초기화 완료 - 임시 경로: {}", tempTessDataDir.getAbsolutePath());
        } catch (Exception e) {
            log.warn("한글 언어팩 설정 실패, 영어만 시도: {}", e.getMessage());
            try {
                tesseract.setLanguage("eng");
            } catch (Exception e2) {
                log.error("Tesseract 초기화 실패: {}", e2.getMessage());
            }
        }
    }

    private File copyTessDataToTemp() throws IOException {
        // 임시 디렉토리 생성
        File tempDir = Files.createTempDirectory("tessdata").toFile();
        tempDir.deleteOnExit();

        // eng.traineddata 복사
        copyResourceToFile("/tessdata/eng.traineddata", new File(tempDir, "eng.traineddata"));

        // kor.traineddata 복사
        copyResourceToFile("/tessdata/kor.traineddata", new File(tempDir, "kor.traineddata"));

        log.info("언어팩 파일들을 임시 폴더로 복사 완료: {}", tempDir.getAbsolutePath());
        return tempDir;
    }

    private void copyResourceToFile(String resourcePath, File targetFile) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath);
             FileOutputStream outputStream = new FileOutputStream(targetFile)) {

            if (inputStream == null) {
                throw new IOException("리소스를 찾을 수 없습니다: " + resourcePath);
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            targetFile.deleteOnExit(); // 애플리케이션 종료시 자동 삭제
            log.debug("파일 복사 완료: {}", targetFile.getAbsolutePath());
        }
    }

    public String extractText(MultipartFile imageFile) {
        File tempFile = null;
        try {
            // 임시 파일로 변환
            tempFile = File.createTempFile("ocr_", ".jpg");
            imageFile.transferTo(tempFile);

            // OCR 실행
            String result = tesseract.doOCR(tempFile);
            log.info("OCR 추출 완료: {}", result);

            return result.trim();

        } catch (Exception e) {
            log.error("OCR 실패: ", e);
            throw new RuntimeException("텍스트 추출 실패", e);
        } finally {
            // 임시 파일 삭제
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
