package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface GatheringRepository extends JpaRepository<GatheringPost, Long> {
    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO(
            gp.id,
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
            gp.gatheringType,
            gp.status,
            gp.headCount,
            gp.place,
            gp.period,
            gp.schedule,
            p.createdAt,
            p.updatedAt,
            false,
            false
        )
        FROM GatheringPost gp
        JOIN gp.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        """)
    Page<GatheringPostResponseDTO> findDTOByContent(@Param("keyword") String keyword,
                                                            Pageable pageable);
    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO(
            gp.id,
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
            gp.gatheringType,
            gp.status,
            gp.headCount,
            gp.place,
            gp.period,
            gp.schedule,
            p.createdAt,
            p.updatedAt,
            false,
            false
        )
        FROM GatheringPost gp
        JOIN gp.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        ORDER BY ps.viewCount DESC
        """)
    Page<GatheringPostResponseDTO> findDTOByContentOrderByViewCount(@Param("keyword") String keyword,
                                                                Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO(
            gp.id,
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
            gp.gatheringType,
            gp.status,
            gp.headCount,
            gp.place,
            gp.period,
            gp.schedule,
            p.createdAt,
            p.updatedAt,
            (SELECT CASE WHEN COUNT(pl.id) > 0 THEN true ELSE false END FROM Like pl WHERE pl.post.id = p.id AND pl.user.id = :userId),
            (SELECT CASE WHEN COUNT(b.id) > 0 THEN true ELSE false END FROM Bookmark b WHERE b.post.id = p.id AND b.user.id = :userId)
        )
        FROM GatheringPost gp
        JOIN gp.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        """)
    Optional<GatheringPostResponseDTO> findDTOByIdAndUserId(@Param("id") Long id, @Param("userId") Long user);

    GatheringPost findByPostId(Long postId);
}
