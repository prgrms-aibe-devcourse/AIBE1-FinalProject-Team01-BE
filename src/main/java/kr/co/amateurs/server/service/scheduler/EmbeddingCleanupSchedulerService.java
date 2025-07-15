package kr.co.amateurs.server.service.scheduler;

import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingCleanupSchedulerService {

    private final PostEmbeddingService postEmbeddingService;

    private static final int CLEANUP_DAYS = 7;

    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupOldEmbeddings() {
        log.info("임베딩 데이터 정리 스케줄링 작업 시작");

        try {
            postEmbeddingService.deleteOldEmbeddings(CLEANUP_DAYS);
            log.info("임베딩 데이터 정리 스케줄링 작업 완료: {}일 이전 데이터 삭제", CLEANUP_DAYS);

        } catch (Exception e) {
            log.error("임베딩 데이터 정리 스케줄링 작업 실패", e);
        }
    }
}