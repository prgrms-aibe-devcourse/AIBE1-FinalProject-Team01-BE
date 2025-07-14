package kr.co.amateurs.server.service.file;

import kr.co.amateurs.server.repository.file.PostImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnreferencedImageClearScheduler {
    private final S3Client s3Client;
    private final PostImageRepository postImageRepository;
    private final FileService fileService;

    @Value("${cloud.aws.cloudfront.domain}")
    public String publicUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 한번에 처리할 작업의 크기(메모리 오버 대비)
    private static final int BATCH_SIZE = 1000;

    // 매주 일 -> 월 사이의 새벽 2시에 실행 - 바꿀 수 있음
    @Scheduled(cron = "0 0 2 ? * MON", zone = "Asia/Seoul")
    public void deleteOrphanImages() {

        Set<String> dbUrls = new HashSet<>(postImageRepository.findAllUrls());
        processBatchDeletion(dbUrls);
    }

    private void processBatchDeletion(Set<String> dbUrls) {
        String continuationToken = null;
        do {
            try {
                ListObjectsV2Response response = s3Client.listObjectsV2(
                        ListObjectsV2Request.builder()
                                .bucket(bucket)
                                .maxKeys(BATCH_SIZE)
                                .prefix("post-images/")
                                .continuationToken(continuationToken)
                                .build()
                );

                List<S3Object> objects = response.contents();

                for (S3Object obj : objects) {
                    if (!obj.key().startsWith("post-images/")) {
                        continue;
                    }
                    String fileUrl = "https://" + publicUrl + "/" + obj.key();
                    if (!dbUrls.contains(fileUrl)) {
                        fileService.deleteFile(fileUrl);
                    }
                }

                continuationToken = response.nextContinuationToken();
            }
            catch (Exception e) {
                log.error("이미지 삭제 스케줄러 실행 중 오류 발생");
            }

        } while (continuationToken != null);
    }
}