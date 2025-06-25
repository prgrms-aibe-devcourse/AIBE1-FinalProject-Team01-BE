package kr.co.amateurs.server.service.file;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.file.FileResponseDTO;
import kr.co.amateurs.server.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    //TODO - 환경변수 채우기

    @Value("${cloud.aws.cloudfront.domain}")
    public String publicUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    public FileResponseDTO uploadFile(MultipartFile file, String directoryPath) throws IOException {
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String folder = (directoryPath != null && !directoryPath.isEmpty()) ? directoryPath : "uploads";
        String fileName = UUID.randomUUID().toString() + extension;
        String key = folder + "/" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        return new FileResponseDTO(
                true,
                "File uploaded",
                publicUrl + "/" + key
        );
    }

    //TODO - 파일 삭제는 필요 시 컨트롤러에 API 추가
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(publicUrl)) {
            return false;
        }

        try {
            String key = fileUrl.substring(publicUrl.length() + 1); // "/" 포함
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            return true;

        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    // TODO - 현재는 타입은 4가지, 용량은 5MB 까지 허용했지만 추가적으로 필요 시 확장 및 변경 가능
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",  // .jpg, .jpeg 둘 다 포함
            "image/png",
            "image/gif",
            "image/webp"
    );
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

}
