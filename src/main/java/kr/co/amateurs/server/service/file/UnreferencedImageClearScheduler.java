package kr.co.amateurs.server.service.file;

import kr.co.amateurs.server.repository.file.PostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
@RequiredArgsConstructor
public class UnreferencedImageClearScheduler {
    private final S3Client s3Client;
    private final PostImageRepository postImageRepository;
    private final FileService fileService;

    @Value("${cloud.aws.cloudfront.domain}")
    public String publicUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 매주 일 -> 월 사이의 새벽 3시에 실행 - 바꿀 수 있음
    @Scheduled(cron = "0 0 3 ? * MON", zone = "Asia/Seoul")
    public void deleteOrphanImages() {
        String continuationToken = null;
        do {
            ListObjectsV2Response listRes = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .continuationToken(continuationToken)
                            .build()
            );

            for (S3Object obj : listRes.contents()) {
                String key     = obj.key();
                String fileUrl = publicUrl + "/" + key;

                boolean existsInDb = postImageRepository.existsByUrl(fileUrl);
                if (!existsInDb) {
                    fileService.deleteFile(fileUrl);
                }
            }

            continuationToken = listRes.nextContinuationToken();
        } while (continuationToken != null);
    }
}