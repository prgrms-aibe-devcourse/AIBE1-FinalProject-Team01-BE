package kr.co.amateurs.server.repository.community;

import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface CommunityRepository extends JpaRepository<CommunityPost, Long> {
    @Query("SELECT cp FROM CommunityPost cp JOIN cp.post p WHERE p.boardType = :boardType AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);

    @Query("SELECT cp FROM CommunityPost cp JOIN cp.post p WHERE p.boardType = :boardType AND p.isDeleted = false AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByContentAndBoardType(@Param("keyword") String keyword,
                                                  @Param("boardType") BoardType boardType,
                                                  Pageable pageable);
}
