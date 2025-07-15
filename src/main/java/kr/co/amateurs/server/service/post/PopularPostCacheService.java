package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.domain.dto.post.PopularPostRequest;
import kr.co.amateurs.server.domain.dto.post.PopularPostResponse;
import kr.co.amateurs.server.repository.post.PopularPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularPostCacheService {

    private final PopularPostRepository popularPostRepository;

    @Cacheable(value = "popularPosts", key = "#limit")
    @Transactional(readOnly = true)
    public List<PopularPostResponse> getCachedPopularPosts(int limit) {
        try {
            log.info("인기글 DB 조회 시작 - Redis 캐시 MISS: limit={}", limit);

            List<PopularPostRequest> requests = popularPostRepository.findLatestPopularPosts(limit);

            List<PopularPostResponse> result = requests.stream()
                    .map(PopularPostResponse::from)
                    .collect(Collectors.toList());

            log.info("인기글 DB 조회 완료: {}개 → Redis 캐싱됨", result.size());
            return result;

        } catch (Exception e) {
            log.error("인기글 조회 실패: limit={}", limit, e);
            return Collections.emptyList();
        }
    }

    @CacheEvict(value = "popularPosts", allEntries = true)
    public void invalidatePopularPostsCache() {
        log.info("인기글 캐시 무효화 완료");
    }

    public List<PopularPostResponse> refreshCache(int limit) {
        log.info("인기글 캐시 수동 갱신 시작: limit={}", limit);
        invalidatePopularPostsCache();
        return getCachedPopularPosts(limit);
    }

    public void logCacheStatus(String operation) {
        log.info("인기글 캐시 작업: {}", operation);
    }
}