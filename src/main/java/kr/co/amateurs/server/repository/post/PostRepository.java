package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardType(BoardType boardType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.boardType = :boardType")
    Page<Post> findByContentAndBoardType(@Param("keyword") String content, @Param("boardType") BoardType boardType, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    int increaseViewCount(@Param("postId") Long postId);

    boolean existsByUserIdAndUpdatedAtAfter(Long userId, LocalDateTime since);

    List<Post> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);

    List<Post> findTop10ByIsDeletedFalseAndCreatedAtAfterOrderByLikeCountDescCreatedAtDesc(LocalDateTime createdAt);
    List<Post> findTop20ByIsDeletedFalseAndCreatedAtAfterOrderByLikeCountDescCreatedAtDesc(LocalDateTime createdAt);

    List<Post> findByUser(User user);
}