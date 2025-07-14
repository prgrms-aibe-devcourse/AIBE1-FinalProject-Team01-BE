package kr.co.amateurs.server.controller.file;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.file.FileResponseDTO;
import kr.co.amateurs.server.service.file.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class FileControllerTest extends AbstractControllerTest {

    @MockitoBean
    private FileService fileService;

    private MockMultipartFile validImageFile;
    private MockMultipartFile validDocumentFile;
    private FileResponseDTO successResponse;

    @BeforeEach
    void setUp() {
        String base64Jpeg = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwA/8A8A";
        byte[] validJpegBytes = Base64.getDecoder().decode(base64Jpeg);
        validImageFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", validJpegBytes
        );

        validDocumentFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test pdf content".getBytes()
        );

        successResponse = new FileResponseDTO(
                true,
                "File uploaded",
                "https://example.com/uploads/test.jpg"
        );
    }

    @Nested
    class 이미지_업로드_테스트 {
        @Test
        void 유효한_이미지_파일_업로드가_성공한다() throws Exception {
            // given
            when(fileService.uploadFile(any(), eq("custom-directory")))
                    .thenReturn(successResponse);

            // when & then
            given()
                    .multiPart("file", validImageFile.getOriginalFilename(),
                            validImageFile.getBytes(), validImageFile.getContentType())
                    .formParam("directory", "custom-directory")
                    .when()
                    .post("/upload/images")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("success", equalTo(true))
                    .body("message", equalTo("File uploaded"))
                    .body("url", equalTo("https://example.com/uploads/test.jpg"));

            verify(fileService).uploadFile(any(), eq("custom-directory"));
        }

        @Test
        void 디렉토리_지정_없이_이미지_업로드_시_기본값을_사용한다() throws Exception {
            // given
            FileResponseDTO defaultResponse = new FileResponseDTO(
                    true,
                    "File uploaded",
                    "https://example.com/post-images/test.jpg"
            );
            when(fileService.uploadFile(any(), eq("post-images")))
                    .thenReturn(defaultResponse);

            // when & then
            given()
                    .multiPart("file", validImageFile.getOriginalFilename(),
                            validImageFile.getBytes(), validImageFile.getContentType())
                    .when()
                    .post("/upload/images")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("url", equalTo("https://example.com/post-images/test.jpg"));

            verify(fileService).uploadFile(any(), eq("post-images"));
        }

        @Test
        void 파일_없이_업로드_요청_시_500_에러가_발생한다() throws IOException {
            // when & then
            given()
                    .formParam("directory", "test")
                    .when()
                    .post("/upload/images")
                    .then()
                    .statusCode(500);

            verify(fileService, never()).uploadFile(any(), any());
        }

        @Test
        void 빈_파일_업로드_시_정상적으로_종료한다() throws IOException {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]
            );

            // when & then
            given()
                    .multiPart("file", emptyFile.getOriginalFilename(),
                            emptyFile.getBytes(), emptyFile.getContentType())
                    .when()
                    .post("/upload/images")
                    .then()
                    .statusCode(HttpStatus.OK.value());
        }

        @Test
        void 서비스에서_예외_발생_시_적절한_에러가_반환된다() throws Exception {
            // given
            when(fileService.uploadFile(any(), any()))
                    .thenThrow(new RuntimeException("File upload failed"));

            // when & then
            given()
                    .multiPart("file", validImageFile.getOriginalFilename(),
                            validImageFile.getBytes(), validImageFile.getContentType())
                    .formParam("directory", "test")
                    .when()
                    .post("/upload/images")
                    .then()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Nested
    class 일반_파일_업로드_테스트 {

        @Test
        void 유효한_문서_파일_업로드가_성공한다() throws Exception {
            // given
            FileResponseDTO documentResponse = new FileResponseDTO(
                    true,
                    "File uploaded",
                    "https://example.com/file/document.pdf"
            );
            when(fileService.uploadFile(any())).thenReturn(documentResponse);

            // when & then
            given()
                    .multiPart("file", validDocumentFile.getOriginalFilename(),
                            validDocumentFile.getBytes(), validDocumentFile.getContentType())
                    .when()
                    .post("/upload/files")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("success", equalTo(true))
                    .body("message", equalTo("File uploaded"))
                    .body("url", equalTo("https://example.com/file/document.pdf"));

            verify(fileService).uploadFile(any());
        }

        @Test
        void 파일_없이_일반_파일_업로드_요청_시_500_에러가_발생한다() throws IOException {
            // when & then
            given()
                    .when()
                    .post("/upload/files")
                    .then()
                    .statusCode(500);

            verify(fileService, never()).uploadFile(any());
        }
    }

    @Nested
    class Content_Type_테스트 {

        @Test
        void 잘못된_Content_Type으로_요청_시_500_에러가_발생한다() {
            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"test\": \"data\"}")
                    .when()
                    .post("/upload/images")
                    .then()
                    .statusCode(500);
        }
    }

}