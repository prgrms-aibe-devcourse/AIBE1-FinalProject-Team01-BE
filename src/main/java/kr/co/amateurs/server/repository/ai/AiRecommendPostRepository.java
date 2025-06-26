package kr.co.amateurs.server.repository.ai;

import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiRecommendPostRepository extends JpaRepository<RecommendedPost, Long> {
    List<RecommendedPost> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}
