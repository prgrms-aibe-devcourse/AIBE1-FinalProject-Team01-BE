package kr.co.amateurs.server.service.file;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.file.FileResponseDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostImage;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.file.PostImageRepository;
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${cloud.aws.cloudfront.domain}")
    public String publicUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final PostImageRepository postImageRepository;

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

    public void deletePostImage(Post post){
        List<PostImage> images = postImageRepository.findByPost(post);
        images.forEach(img -> deleteFile(img.getImageUrl()));
        postImageRepository.deleteAll(images);
    }

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

    private static final Pattern IMG_SRC_PATTERN =
            Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    public List<String> extractImageUrls(String content) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = IMG_SRC_PATTERN.matcher(content);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }
    public void savePostImage(Post post, List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            postImageRepository.save(PostImage.builder()
                    .post(post)
                    .imageUrl(imageUrl)
                    .build()
            );
        }
    }

    // 소셜가입 유저의 프로필 이미지를 다운로드하여 S3에 업로드
    public String downloadAndUploadToS3(String imageUrl, String directoryPath) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        if (imageUrl.startsWith(publicUrl)) {
            return imageUrl;
        }

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (connection.getResponseCode() != 200) {
                log.warn("이미지 다운로드 실패: {}, 응답코드: {}", imageUrl, connection.getResponseCode());
                return imageUrl;
            }

            String contentType = connection.getContentType();

            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                log.warn("지원하지 않는 이미지 타입: {} for URL: {}", contentType, imageUrl);
                return imageUrl;
            }

            String extension = contentType.equals("image/jpeg") ? ".jpg" :
                    "." + contentType.substring(contentType.lastIndexOf("/") + 1);

            String folder = (directoryPath != null && !directoryPath.isEmpty()) ? directoryPath : "profile-images";
            String fileName = UUID.randomUUID().toString() + extension;
            String key = folder + "/" + fileName;

            try (InputStream inputStream = connection.getInputStream()) {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build();

                s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));

                String s3Url = publicUrl + "/" + key;
                log.info("소셜 이미지 S3 업로드 성공: {} -> {}", imageUrl, s3Url);
                return s3Url;
            }

        } catch (Exception e) {
            log.error("이미지 S3 업로드 실패: {} - {}", imageUrl, e.getMessage(), e);
            return imageUrl;
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
