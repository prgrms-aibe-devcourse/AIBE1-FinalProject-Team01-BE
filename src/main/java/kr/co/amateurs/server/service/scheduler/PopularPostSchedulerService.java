package kr.co.amateurs.server.service.scheduler;

import kr.co.amateurs.server.service.post.PopularPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularPostSchedulerService {

    private final PopularPostService popularPostService;

    @Scheduled(cron = "0 0 3 * * *")
    public void updatePopularPosts() {
        log.info("인기글 스케줄링 작업 시작");
        try {
            popularPostService.calculateAndSavePopularPosts();
            log.info("인기글 스케줄링 작업 완료");
        } catch (Exception e) {
            log.error("인기글 스케줄링 작업 실패", e);
        }
    }
}