package kr.co.amateurs.server.repository.bookmark;

import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        where b.user.id = :userId
        order by p.viewCount desc
    """)
    Page<Bookmark> getBookmarkPostByUserOrderByViewCountDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );


    // TODO - 메서드 이름으로 가능하면 변경
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.post.id = :postId AND b.user.id = :userId")
    int deleteByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    Optional<Bookmark> findByPost_IdAndUser_Id(Long postId, Long id);
}
