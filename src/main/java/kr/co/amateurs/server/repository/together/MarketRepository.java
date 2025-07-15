package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface MarketRepository extends JpaRepository<MarketItem, Long> {
    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO(
            mi.id,
            p.id,
            p.isBlinded,
            u.nickname,
            u.devcourseName,
            u.devcourseBatch,
            u.imageUrl,
            p.title,
            p.content,
            p.tags,
            ps.viewCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            p.likeCount,
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            mi.status,
            mi.price,
            mi.place,
            (SELECT COALESCE(pi.imageUrl, '') FROM PostImage pi WHERE pi.post.id = p.id ORDER BY pi.id ASC LIMIT 1),
            p.createdAt,
            p.updatedAt,
            false,
            false
        )
        FROM MarketItem mi
        JOIN mi.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        """)
    Page<MarketPostResponseDTO> findDTOByContent(@Param("keyword") String keyword,
                                                 Pageable pageable);
    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO(
            mi.id,
            p.id,
            p.isBlinded,
            u.nickname,
            u.devcourseName,
            u.devcourseBatch,
            u.imageUrl,
            p.title,
            p.content,
            p.tags,
            ps.viewCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            p.likeCount,
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            mi.status,
            mi.price,
            mi.place,
            (SELECT COALESCE(pi.imageUrl, '') FROM PostImage pi WHERE pi.post.id = p.id ORDER BY pi.id ASC LIMIT 1),
            p.createdAt,
            p.updatedAt,
            false,
            false
        )
        FROM MarketItem mi
        JOIN mi.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        ORDER BY ps.viewCount DESC
        """)
    Page<MarketPostResponseDTO> findDTOByContentOrderByViewCount(@Param("keyword") String keyword,
                                                                    Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO(
            mi.id,
            p.id,
            p.isBlinded,
            u.nickname,
            u.devcourseName,
            u.devcourseBatch,
            u.imageUrl,
            p.title,
            p.content,
            p.tags,
            ps.viewCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            p.likeCount,
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            mi.status,
            mi.price,
            mi.place,
            "",
            p.createdAt,
            p.updatedAt,
            (SELECT CASE WHEN COUNT(pl.id) > 0 THEN true ELSE false END FROM Like pl WHERE pl.post.id = p.id AND pl.user.id = :userId),
            (SELECT CASE WHEN COUNT(b.id) > 0 THEN true ELSE false END FROM Bookmark b WHERE b.post.id = p.id AND b.user.id = :userId)
        )
        FROM MarketItem mi
        JOIN mi.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE mi.id = :id
        """)
    Optional<MarketPostResponseDTO> findDTOByIdAndUserId(@Param("id") Long id, @Param("userId") Long user);


    MarketItem findByPostId(Long postId);
}
