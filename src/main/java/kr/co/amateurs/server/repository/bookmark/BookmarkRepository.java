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

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
//    Page<Bookmark> getBookmarkPostByUser(Long userId, Pageable pageable);
//
//    Page<Bookmark> getBookmarkPostByUserOrderByLikeCountDesc(Long userId, Pageable pageable);
//
//    Page<Bookmark> getBookmarkPostByUserOrderByViewCountDesc(Long userId, Pageable pageable);


    @Transactional
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.post.id = :postId AND b.user.id = :userId")
    int deleteByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);
}
