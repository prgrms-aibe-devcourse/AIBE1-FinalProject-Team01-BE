package kr.co.amateurs.server.repository.ai;

import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendedPostRepository extends JpaRepository<RecommendedPost, Long> {

    // 사용자별 추천 게시글 조회 (최신순)
    List<RecommendedPost> findByUserOrderByCreatedAtDesc(User user);

    List<RecommendedPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 사용자별 최근 N개 추천 조회
    @Query("SELECT rp FROM RecommendedPost rp WHERE rp.user.id = :userId ORDER BY rp.createdAt DESC")
    List<RecommendedPost> findTopNByUserId(@Param("userId") Long userId, Pageable pageable);

    // 특정 사용자-게시글 조합의 추천 존재 여부 확인
    Optional<RecommendedPost> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // 사용자별 추천 개수 조회
    @Query("SELECT COUNT(rp) FROM RecommendedPost rp WHERE rp.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // 특정 게시글이 추천된 횟수 조회
    @Query("SELECT COUNT(rp) FROM RecommendedPost rp WHERE rp.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    // 특정 시간 이전의 추천들 삭제 (배치 정리용)
    @Modifying
    @Query("DELETE FROM RecommendedPost rp WHERE rp.createdAt < :cutoffTime")
    int deleteOldRecommendations(@Param("cutoffTime") LocalDateTime cutoffTime);

    // 오늘 생성된 추천들 조회
    @Query("SELECT rp FROM RecommendedPost rp WHERE rp.createdAt >= :startOfDay AND rp.createdAt < :endOfDay")
    List<RecommendedPost> findTodayRecommendations(@Param("startOfDay") LocalDateTime startOfDay,
                                                  @Param("endOfDay") LocalDateTime endOfDay);

    // 사용자의 기존 추천 모두 삭제 (새로운 추천 생성 전)
    @Modifying
    @Query("DELETE FROM RecommendedPost rp WHERE rp.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
