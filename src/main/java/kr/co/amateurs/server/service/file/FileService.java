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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
                "https://" + publicUrl + "/" + key
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


    // TODO - 현재는 타입은 4가지, 용량은 5MB 까지 허용했지만 추가적으로 필요 시 확장 및 변경 가능
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",  // .jpg, .jpeg 둘 다 포함
            "image/png",
            "image/gif",
            "image/webp"
    );
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        if (!hasValidMagicBytes(file) || !isReadableImage(file)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private boolean hasValidMagicBytes(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            if (is.read(header) != header.length) return false;
            // JPEG
            if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) return true;
            // PNG
            if (header[0] == (byte)0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') return true;
            // GIF
            if (header[0]=='G' && header[1]=='I' && header[2]=='F' && header[3]=='8') return true;
            // WebP
            if (header[0]=='R' && header[1]=='I' && header[2]=='F' && header[3]=='F') {
                byte[] webp = new byte[4];
                is.skip(4);
                if (is.read(webp)==4 && new String(webp).equals("WEBP")) return true;
            }
            return false;
        }
    }
    private boolean isReadableImage(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            BufferedImage img = ImageIO.read(is);
            return img != null;    // 디코딩 실패 시 null 리턴
        } catch (IOException e) {
            return false;
        }
    }

}
