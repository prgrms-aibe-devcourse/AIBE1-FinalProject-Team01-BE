package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.domain.dto.post.PopularPostRequest;
import kr.co.amateurs.server.domain.dto.post.PopularPostResponse;
import kr.co.amateurs.server.domain.entity.post.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.co.amateurs.server.repository.post.PopularPostRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularPostService {
    private final PopularPostRepository popularPostRepository;
    private final PostService postService;

    private static final double VIEW_WEIGHT = 0.4;
    private static final double LIKE_WEIGHT = 0.4;
    private static final double COMMENT_WEIGHT = 0.2;

    private static final int LIKE_MULTIPLIER = 10;
    private static final int COMMENT_MULTIPLIER = 10;

    @Transactional
    public void calculateAndSavePopularPosts() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDate today = LocalDate.now();

        log.info("인기글 계산 시작: 기준일자={}, 대상기간=3일", today);

        List<PopularPostRequest> recentPosts = popularPostRepository.findRecentPostsWithCounts(threeDaysAgo);

        if (recentPosts.isEmpty()) {
            log.warn("3일 이내 게시글이 없습니다.");
            return;
        }

        List<PopularPostRequest> popularPosts = recentPosts.stream()
                .map(post -> calculatePopularityScore(post, today))
                .filter(post -> !post.isBlinded() && !post.isDeleted())
                .sorted((p1, p2) -> Double.compare(p2.popularityScore(), p1.popularityScore()))
                .limit(10)
                .collect(Collectors.toList());

        popularPostRepository.savePopularPosts(popularPosts);
        log.info("인기글 계산 완료: 총 {}개 게시글 처리", popularPosts.size());

        popularPostRepository.deleteBeforeDate(today);
    }

    private PopularPostRequest calculatePopularityScore(PopularPostRequest post, LocalDate calculatedDate) {
        double viewScore = Math.sqrt(post.viewCount());
        double likeScore = Math.sqrt(post.likeCount() * LIKE_MULTIPLIER);
        double commentScore = Math.sqrt(post.commentCount() * COMMENT_MULTIPLIER);

        double popularityScore = (viewScore * VIEW_WEIGHT) +
                (likeScore * LIKE_WEIGHT) +
                (commentScore * COMMENT_WEIGHT);

        PopularPostRequest postWithScore = PopularPostRequest.withScore(
                post, popularityScore, calculatedDate
        );

        try {
            Long boardId = postService.getBoardId(post.postId(), post.boardType());
            if (boardId == null) {
                boardId = post.postId();
                log.warn("BoardId 조회 실패, postId로 대체: postId={}, boardType={}",
                        post.postId(), post.boardType());
            }

            var status = popularPostRepository.getBoardStatus(post.postId());

            return PopularPostRequest.withBoardIdAndStatus(
                    postWithScore,
                    boardId,
                    status.isBlinded(),
                    status.isDeleted()
            );
        } catch (Exception e) {
            log.warn("BoardId/상태 조회 실패: postId={}, boardType={}, error={}",
                    post.postId(), post.boardType(), e.getMessage());
            return PopularPostRequest.withBoardIdAndStatus(
                    postWithScore, post.postId(), false, false
            );
        }
    }

    @Transactional(readOnly = true)
    public List<PopularPostResponse> getPopularPosts(int limit) {
        List<PopularPostRequest> requests = popularPostRepository.findLatestPopularPosts(limit);
        return requests.stream()
                .map(PopularPostResponse::from)
                .collect(Collectors.toList());
    }
}

