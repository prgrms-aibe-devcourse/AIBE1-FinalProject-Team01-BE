package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewCountService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostStatisticsRepository postStatisticsRepository;

    private static final String VIEW_COUNT_KEY = "post:view:count:";
    private static final String VIEW_IP_KEY = "post:view:ip:";
    private static final String VIEW_COUNT_HASH = "post:view:counts";

    /**
     * 조회수 증가 (IP 기반 중복 방지)
     * @param postId 게시글 ID
     * @param clientIp 클라이언트 IP
     * @return 증가 여부
     */
    public boolean incrementViewCount(Long postId, String clientIp) {
        String ipKey = VIEW_IP_KEY + postId + ":" + clientIp;

        Boolean isNewView = redisTemplate.opsForValue()
                .setIfAbsent(ipKey, "1", Duration.ofHours(1));

        if (Boolean.TRUE.equals(isNewView)) {
            redisTemplate.opsForHash().increment(VIEW_COUNT_HASH, postId.toString(), 1);
            return true;
        }

        return false;
    }

    /**
     * Redis에서 실시간 조회수 조회
     * @param postId 게시글 ID
     * @return 현재 조회수
     */
    public Long getViewCount(Long postId) {
        Object redisCount = redisTemplate.opsForHash().get(VIEW_COUNT_HASH, postId.toString());
        Long incrementCount = redisCount != null ? Long.valueOf(redisCount.toString()) : 0L;

        Long baseCount = postStatisticsRepository.findById(postId)
                .map(PostStatistics::getViewCount)
                .map(Integer::longValue)
                .orElse(0L);

        return baseCount + incrementCount;
    }

    /**
     * Redis의 조회수 데이터를 DB에 동기화
     */
    @Transactional
    public void syncViewCountsToDB() {
        try {
            Set<Object> postIds = redisTemplate.opsForHash().keys(VIEW_COUNT_HASH);

            for (Object postIdObj : postIds) {
                try {
                    Long postId = Long.valueOf(postIdObj.toString());
                    Object countObj = redisTemplate.opsForHash().get(VIEW_COUNT_HASH, postIdObj);

                    if (countObj != null) {
                        Long incrementCount = Long.valueOf(countObj.toString());

                        postStatisticsRepository.findById(postId)
                                .ifPresent(stats -> {
                                    int newCount = stats.getViewCount() + incrementCount.intValue();
                                    stats.updateViewCount(newCount);
                                    postStatisticsRepository.save(stats);
                                });
                    }
                } catch (Exception e) {
                    log.error("개별 게시글 조회수 동기화 실패: postId={}", postIdObj.toString(), e);
                }
            }

            redisTemplate.delete(VIEW_COUNT_HASH);
        } catch (Exception e) {
            log.error("조회수 동기화 중 오류 발생", e);
        }
    }

    /**
     * 특정 게시글의 Redis 조회수 데이터 초기화
     * @param postId 게시글 ID
     */
    public void clearViewCountCache(Long postId) {
        redisTemplate.opsForHash().delete(VIEW_COUNT_HASH, postId.toString());

        String pattern = VIEW_IP_KEY + postId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
