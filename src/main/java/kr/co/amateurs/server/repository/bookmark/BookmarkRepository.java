package kr.co.amateurs.server.repository.bookmark;

import kr.co.amateurs.server.domain.dto.bookmark.BookmarkCount;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    @Query("""
        select b
        from Bookmark b
        join fetch b.post p
        where b.user.id = :userId
        order by p.createdAt desc
    """)
    Page<Bookmark> getBookmarkPostByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
        select b
        from Bookmark b
        join fetch b.post p
        where b.user.id = :userId
        order by p.likeCount desc
    """)
    Page<Bookmark> getBookmarkPostByUserOrderByLikeCountDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
        select b
        from Bookmark b
        join fetch b.post p
        join PostStatistics ps on ps.postId = p.id
        where b.user.id = :userId
        order by ps.viewCount desc
    """)
    Page<Bookmark> getBookmarkPostByUserOrderByViewCountDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    void deleteByUserIdAndPostId(Long userId, Long postId);

    boolean existsByPost_IdAndUser_Id(Long postId, Long id);

    int countByPostId(@Param("postId") Long postId);

    @Query("""
        SELECT b.post.id as postId, COUNT(b) as bookmarkCount
        FROM Bookmark b 
        WHERE b.post.id IN :postIds 
        GROUP BY b.post.id
    """)
    List<BookmarkCount> countByPostIds(@Param("postIds") List<Long> postIds);

    List<Bookmark> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    void deleteByPost_Id(Long postId);
}
