package kr.co.amateurs.server.service.scheduler;

import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.service.ai.PostRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiPostRecommendScehdularService {
    private final PostRecommendService postRecommendService;
    private final AiProfileRepository aiProfileRepository;

    private static final int BATCH_SIZE = 20;
    private static final int RECOMMENDATION_LIMIT = 12;

    @Scheduled(cron = "0 0 5 * * *")
    public void updateAllUsersRecommendations() {
        log.info("전체 사용자 맞춤 추천 게시글 업데이트 시작");

        try {
            int totalProcessed = 0;
            int totalUpdated = 0;
            int page = 0;

            Page<AiProfile> profilePage;
            do {
                Pageable pageable = PageRequest.of(page, BATCH_SIZE);
                profilePage = aiProfileRepository.findAll(pageable);

                for (AiProfile profile : profilePage.getContent()) {
                    totalProcessed++;

                    try {
                        postRecommendService.saveRecommendationsToDB(profile.getUser().getId(), RECOMMENDATION_LIMIT);
                        totalUpdated++;
                        log.info("사용자 맞춤 추천 생성 완료: userId={}", profile.getUser().getId());
                    } catch (Exception e) {
                        log.error("사용자 맞춤 추천 생성 실패: userId={}", profile.getUser().getId(), e);
                    }
                }

                page++;
                log.info("배치 처리 완료: page={}, processed={}, updated={}", page, totalProcessed, totalUpdated);

            } while (profilePage.hasNext());

            log.info("전체 사용자 맞춤 추천 업데이트 완료: 총 처리={}, 총 업데이트={}", totalProcessed, totalUpdated);

        } catch (Exception e) {
            log.error("전체 사용자 맞춤 추천 업데이트 실패", e);
        }
    }
}
