package kr.co.amateurs.server.repository.it;

import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.ITPost;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ITRepository extends JpaRepository<ITPost, Long> {
    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id,
            p.id,
            p.title,
            p.content,
            u.nickname,
            u.imageUrl,
            u.devcourseName,
            u.devcourseBatch,
            p.boardType,
            p.isBlinded,
            ps.viewCount,
            p.likeCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt,
            p.updatedAt,
            p.tags,
            false,
            false
        )
        FROM ITPost ip
        JOIN ip.post p
        JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        WHERE p.boardType = :boardType
        """)
    Page<ITResponseDTO> findDTOByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id,
            p.id,
            p.title,
            p.content,
            u.nickname,
            u.imageUrl,
            u.devcourseName,
            u.devcourseBatch,
            p.boardType,
            p.isBlinded,
            ps.viewCount,
            p.likeCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt,
            p.updatedAt,
            p.tags,
            false,
            false
        )
        FROM ITPost ip
        JOIN ip.post p
        JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        WHERE p.boardType = :boardType
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        """)
    Page<ITResponseDTO> findDTOByContentAndBoardType(@Param("keyword") String keyword,
                                                     @Param("boardType") BoardType boardType,
                                                     Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id, p.id, p.title, p.content, u.nickname, u.imageUrl,
            u.devcourseName, u.devcourseBatch, p.boardType, p.isBlinded, ps.viewCount,
            p.likeCount, 
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id),
            p.createdAt, p.updatedAt, p.tags,
            (SELECT CASE WHEN COUNT(pl2.id) > 0 THEN true ELSE false END FROM Like pl2 WHERE pl2.post.id = p.id AND pl2.user.id = :userId),
            (SELECT CASE WHEN COUNT(b3.id) > 0 THEN true ELSE false END FROM Bookmark b3 WHERE b3.post.id = p.id AND b3.user.id = :userId)
        )
        FROM ITPost ip
        JOIN ip.post p JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        WHERE ip.id = :itId
        """)
    Optional<ITResponseDTO> findDTOByIdForUser(@Param("itId") Long itId,
                                               @Param("userId") Long userId);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id, p.id, p.title, p.content, u.nickname, u.imageUrl,
            u.devcourseName, u.devcourseBatch, p.boardType, p.isBlinded, ps.viewCount,
            p.likeCount, 
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt, p.updatedAt, p.tags, false, false
        )
        FROM ITPost ip
        JOIN ip.post p JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        WHERE ip.id = :itId
        """)
    Optional<ITResponseDTO> findDTOByIdForGuest(@Param("itId") Long itId);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id,
            p.id,
            p.title,
            p.content,
            u.nickname,
            u.imageUrl,
            u.devcourseName,
            u.devcourseBatch,
            p.boardType,
            p.isBlinded,
            ps.viewCount,
            p.likeCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt,
            p.updatedAt,
            p.tags,
            false,
            false
        )
        FROM ITPost ip
        JOIN ip.post p
        JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        WHERE p.boardType = :boardType
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        ORDER BY ps.viewCount DESC
        """)
    Page<ITResponseDTO> findDTOByContentAndBoardTypeOrderByViewCount(@Param("keyword") String keyword,
                                                                     @Param("boardType") BoardType boardType,
                                                                     Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id,
            p.id,
            p.title,
            p.content,
            u.nickname,
            u.imageUrl,
            u.devcourseName,
            u.devcourseBatch,
            p.boardType,
            p.isBlinded,
            ps.viewCount,
            p.likeCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt,
            p.updatedAt,
            p.tags,
            false,
            false
        )
        FROM ITPost ip
        JOIN ip.post p
        JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        WHERE p.boardType = :boardType
        ORDER BY ps.viewCount DESC
        """)
    Page<ITResponseDTO> findDTOByBoardTypeOrderByViewCount(@Param("boardType") BoardType boardType,
                                                           Pageable pageable);
}