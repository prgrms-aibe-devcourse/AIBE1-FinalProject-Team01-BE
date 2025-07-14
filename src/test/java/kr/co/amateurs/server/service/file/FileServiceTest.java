package kr.co.amateurs.server.service.file;

import kr.co.amateurs.server.domain.dto.file.FileResponseDTO;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.file.PostImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FileServiceTest {

    @MockitoBean
    private S3Client s3Client;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private FileService fileService;

    private MockMultipartFile validJpegFile;
    private MockMultipartFile validPngFile;
    private MockMultipartFile invalidFile;
    private MockMultipartFile emptyFile;
    private MockMultipartFile oversizedFile;

    @BeforeEach
    void setUp() {
        String base64Png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        byte[] validPngBytes = Base64.getDecoder().decode(base64Png);
        validPngFile = new MockMultipartFile(
                "file", "test.png", "image/png", validPngBytes
        );

        String base64Jpeg = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwA/8A8A";
        byte[] validJpegBytes = Base64.getDecoder().decode(base64Jpeg);
        validJpegFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", validJpegBytes
        );

        invalidFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "invalid content".getBytes()
        );

        emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        byte[] oversizedContent = new byte[6 * 1024 * 1024];
        oversizedContent[0] = (byte) 0xFF;
        oversizedContent[1] = (byte) 0xD8;
        oversizedContent[2] = (byte) 0xFF;
        oversizedContent[3] = (byte) 0xE0;
        oversizedFile = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", oversizedContent
        );
    }

    @Test
    void 유효한_JPEG_이미지_파일_업로드가_성공한다() throws IOException {
        // when
        FileResponseDTO result = fileService.uploadFile(validJpegFile, "test-directory");

        // then
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("File uploaded");
        assertThat(result.url()).contains("/test-directory/");
        assertThat(result.url()).endsWith(".jpg");

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 유효한_PNG_이미지_파일_업로드가_성공한다() throws IOException {
        // when
        FileResponseDTO result = fileService.uploadFile(validPngFile, "test-directory");

        // then
        assertThat(result.success()).isTrue();
        assertThat(result.url()).endsWith(".png");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 디렉토리_지정_없이_파일_업로드_시_기본_uploads_디렉토리를_사용한다() throws IOException {
        // when
        FileResponseDTO result = fileService.uploadFile(validJpegFile, "");

        // then
        assertThat(result.url()).contains("/uploads/");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 빈_파일_업로드_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> fileService.uploadFile(emptyFile, "test"))
                .isInstanceOf(CustomException.class);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 잘못된_파일_타입_업로드_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> fileService.uploadFile(invalidFile, "test"))
                .isInstanceOf(CustomException.class);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 파일_크기_초과_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> fileService.uploadFile(oversizedFile, "test"))
                .isInstanceOf(CustomException.class);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 기본_업로드_메서드로_파일을_file_디렉토리에_업로드한다() throws IOException {
        // when
        FileResponseDTO result = fileService.uploadFile(validJpegFile);

        // then
        assertThat(result.success()).isTrue();
        assertThat(result.url()).contains("/file/");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 잘못된_URL로_파일_삭제_시_false를_반환한다() {
        // given
        String invalidUrl = "https://other-domain.com/test-file.jpg";

        // when
        boolean result = fileService.deleteFile(invalidUrl);

        // then
        assertThat(result).isFalse();
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void null_URL로_파일_삭제_시_false를_반환한다() {
        // when
        boolean result = fileService.deleteFile(null);

        // then
        assertThat(result).isFalse();
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void S3_예외_발생_시_파일_삭제가_실패한다() {
        // given
        String fileUrl = "https://test.cloudfront.net/uploads/test-file.jpg";
        doThrow(new RuntimeException("S3 error")).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // when
        boolean result = fileService.deleteFile(fileUrl);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void HTML_컨텐츠에서_이미지_URL을_정확히_추출한다() {
        // given
        String htmlContent = """
            <div>
                <img src="https://example.com/image1.jpg" alt="image1"/>
                <p>Some text</p>
                <img src='https://example.com/image2.png' alt='image2'/>
                <img src="https://example.com/image3.gif"/>
            </div>
            """;

        // when
        List<String> urls = fileService.extractImageUrls(htmlContent);

        // then
        assertThat(urls).hasSize(3);
        assertThat(urls).contains(
                "https://example.com/image1.jpg",
                "https://example.com/image2.png",
                "https://example.com/image3.gif"
        );
    }

    @Test
    void 이미지가_없는_HTML에서_빈_리스트를_반환한다() {
        // given
        String htmlContent = "<div><p>No images here</p></div>";

        // when
        List<String> urls = fileService.extractImageUrls(htmlContent);

        // then
        assertThat(urls).isEmpty();
    }

    @Test
    void 빈_이미지_URL로_다운로드_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> fileService.downloadImageFromUrl(""))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void null_이미지_URL로_다운로드_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> fileService.downloadImageFromUrl(null))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 잘못된_형식의_URL로_다운로드_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> fileService.downloadImageFromUrl("invalid-url"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 파일명이_없는_파일_업로드도_정상_처리된다() throws IOException {
        // given
        byte[] validJpegBytes = new byte[1024];
        validJpegBytes[0] = (byte) 0xFF;
        validJpegBytes[1] = (byte) 0xD8;
        validJpegBytes[2] = (byte) 0xFF;
        validJpegBytes[3] = (byte) 0xE0;
        MockMultipartFile fileWithoutName = new MockMultipartFile(
                "file", null, "image/jpeg", validJpegBytes
        );

        // then
        assertThatThrownBy(() -> fileService.uploadFile(fileWithoutName, "test"))
                .isInstanceOf(CustomException.class);
    }
}
