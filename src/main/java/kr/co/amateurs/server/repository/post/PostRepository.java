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

    List<Post> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);

    List<Post> findByUser(User user);
    boolean existsByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    List<Post> findByUserIdIn(List<Long> followingUserId);
}