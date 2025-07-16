package kr.co.amateurs.server.service.scheduler;

import kr.co.amateurs.server.service.post.ViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountScheduler {

    private final ViewCountService viewCountService;

    /**
     * 5분마다 Redis의 조회수를 DB에 동기화
     */
    @Scheduled(fixedRate = 300000)
    @Async
    public void syncViewCountsToDatabase() {

        try {
            viewCountService.syncViewCountsToDB();
        } catch (Exception e) {
            log.error("조회수 동기화 오류 발생");
        }
    }
}
