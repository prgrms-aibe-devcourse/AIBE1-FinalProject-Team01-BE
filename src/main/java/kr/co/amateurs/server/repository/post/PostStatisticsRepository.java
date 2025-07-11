package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostStatisticsRepository extends JpaRepository<PostStatistics, Long> {

    @Query("SELECT ps FROM PostStatistics ps WHERE ps.postId = :postId")
    PostStatistics findByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStatistics ps SET ps.viewCount = :viewCount WHERE ps.postId = :postId")
    void updateViewCount(@Param("postId") Long postId, @Param("viewCount") Integer viewCount);

    @Modifying
    @Query("UPDATE PostStatistics ps SET ps.viewCount = ps.viewCount + :increment WHERE ps.postId = :postId")
    void incrementViewCount(@Param("postId") Long postId, @Param("increment") Integer increment);

    @Modifying
    @Query(value = "INSERT INTO post_statistics (post_id, view_count) VALUES (:postId, 0) " +
            "ON DUPLICATE KEY UPDATE view_count = view_count", nativeQuery = true)
    void createIfNotExists(@Param("postId") Long postId);

    List<PostStatistics> findByPostIdIn(List<Long> postIds);
}
