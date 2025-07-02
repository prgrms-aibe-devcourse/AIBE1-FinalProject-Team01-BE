package kr.co.amateurs.server.service.scheduler;

import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.ai.AiProfileService;
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
public class AiProfileSchedulerService {

    private final AiProfileService aiProfileService;
    private final UserRepository userRepository;

    private static final int BATCH_SIZE = 20;
    private static final int ACTIVITY_CHECK_DAYS = 3;

    @Scheduled(cron = "0 0 4 */3 * *")
    public void updateActiveUsersProfiles() {
        log.info("AI 프로필 업데이트 스케줄링 작업 시작");

        try {
            int totalProcessed = 0;
            int totalUpdated = 0;
            int page = 0;

            Page<User> userPage;
            do {
                Pageable pageable = PageRequest.of(page, BATCH_SIZE);
                userPage = userRepository.findByRoleIn(List.of(Role.GUEST, Role.STUDENT), pageable);

                for (User user : userPage.getContent()) {
                    totalProcessed++;

                    try {
                        if (aiProfileService.hasRecentActivity(user.getId(), ACTIVITY_CHECK_DAYS)) {
                            log.info("사용자 활동 감지, 프로필 업데이트 시작: userId={}", user.getId());
                            aiProfileService.generateCompleteUserProfile(user.getId());
                            totalUpdated++;
                            log.info("사용자 프로필 업데이트 완료: userId={}", user.getId());
                        }
                    } catch (Exception e) {
                        log.error("사용자 프로필 업데이트 실패: userId={}", user.getId(), e);
                    }
                }

                page++;
                log.info("배치 처리 완료: page={}, processed={}, updated={}", page, totalProcessed, totalUpdated);

            } while (userPage.hasNext());

            log.info("AI 프로필 업데이트 스케줄링 작업 완료: 총 처리={}, 총 업데이트={}", totalProcessed, totalUpdated);

        } catch (Exception e) {
            log.error("AI 프로필 업데이트 스케줄링 작업 실패", e);
        }
    }
}