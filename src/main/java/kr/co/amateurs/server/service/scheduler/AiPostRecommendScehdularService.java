package kr.co.amateurs.server.service.scheduler;

import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.service.ai.PostRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiPostRecommendScehdularService {
    private final PostRecommendService postRecommendService;
    private final AiProfileRepository aiProfileRepository;

    private static final int RECOMMENDATION_LIMIT = 10;

    @Scheduled(cron = "0 0 5 * * *")
    public void updateAllUsersRecommendations() {
        log.info("전체 사용자 맞춤 추천 게시글 업데이트 시작");

        try {
            List<AiProfile> profiles = aiProfileRepository.findAll();
            int totalProcessed = 0;
            int totalUpdated = 0;

            for (AiProfile profile : profiles) {
                try {
                    totalProcessed++;
                    postRecommendService.saveRecommendationsToDB(profile.getUser().getId(), 10);
                    totalUpdated++;
                    log.info("사용자 맞춤 추천 생성 완료: userId={}", profile.getUser().getId());
                } catch (Exception e) {
                    log.error("사용자 맞춤 추천 생성 실패: userId={}", profile.getUser().getId(), e);
                }
            }

            log.info("전체 사용자 맞춤 추천 업데이트 완료: 처리={}, 성공={}", totalProcessed, totalUpdated);
        } catch (Exception e) {
            log.error("전체 사용자 맞춤 추천 업데이트 실패", e);
        }
    }
}
