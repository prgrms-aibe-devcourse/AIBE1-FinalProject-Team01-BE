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
                CAST(COUNT(c) AS int),
                CAST(COUNT(b) AS int),
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
            LEFT JOIN Comment c ON c.postId = p.id AND c.isDeleted = false
            LEFT JOIN Bookmark b ON b.post.id = p.id AND b.user.id = u.id
            WHERE p.boardType = :boardType
            GROUP BY p.id, p.title, p.content, u.nickname, u.imageUrl,
                     u.devcourseName, u.devcourseBatch, p.boardType, ps.viewCount,
                     p.likeCount, p.createdAt, p.updatedAt, p.tags
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
                CAST(COUNT(c) AS int),
                CAST(COUNT(b) AS int),
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
            LEFT JOIN Comment c ON c.postId = p.id AND c.isDeleted = false
            LEFT JOIN Bookmark b ON b.post.id = p.id AND b.user.id = u.id
            WHERE p.boardType = :boardType
              AND (:keyword IS NULL
                   OR :keyword = ''
                   OR p.title LIKE CONCAT('%', :keyword, '%')
                   OR p.content LIKE CONCAT('%', :keyword, '%'))
            GROUP BY p.id, p.title, p.content, u.nickname, u.imageUrl,
                     u.devcourseName, u.devcourseBatch, p.boardType, ps.viewCount,
                     p.likeCount, p.createdAt, p.updatedAt, p.tags
            """)
    Page<ITResponseDTO> findDTOByContentAndBoardType(@Param("keyword") String keyword,
                                                     @Param("boardType") BoardType boardType,
                                                     Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id, p.id, p.title, p.content, u.nickname, u.imageUrl,
            u.devcourseName, u.devcourseBatch, p.boardType,p.isBlinded, ps.viewCount,
            p.likeCount, CAST(COUNT(DISTINCT c.id) AS int), CAST(COUNT(DISTINCT b.id) AS int), p.createdAt,
            p.updatedAt, p.tags,
            CASE WHEN pl.id IS NOT NULL THEN true ELSE false END,
            CASE WHEN b.id IS NOT NULL THEN true ELSE false END
        )
        FROM ITPost ip
        JOIN ip.post p JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        LEFT JOIN Comment c ON c.postId = p.id AND c.isDeleted = false
        LEFT JOIN Like pl ON pl.post.id = p.id AND pl.user.id = :userId
        LEFT JOIN Bookmark b ON b.post.id = p.id AND b.user.id = :userId
        WHERE ip.id = :itId
        GROUP BY ip.id, p.id, u.id, pl.id, b.id
        """)
    Optional<ITResponseDTO> findDTOByIdForUser(@Param("itId") Long itId,
                                                      @Param("userId") Long userId);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.it.ITResponseDTO(
            ip.id, p.id, p.title, p.content, u.nickname, u.imageUrl,
            u.devcourseName, u.devcourseBatch, p.boardType,p.isBlinded, ps.viewCount,
            p.likeCount, CAST(COUNT(DISTINCT c.id) AS int), CAST(COUNT(DISTINCT b.id) AS int), p.createdAt,
            p.updatedAt, p.tags, false, false
        )
        FROM ITPost ip
        JOIN ip.post p JOIN p.user u
        JOIN PostStatistics ps ON p.id = ps.postId
        LEFT JOIN Comment c ON c.postId = p.id AND c.isDeleted = false
        LEFT JOIN Bookmark b ON b.post.id = p.id AND b.user.id = u.id
        WHERE ip.id = :itId
        GROUP BY ip.id, p.id, u.id
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
                CAST(COUNT(c) AS int),
                CAST(COUNT(b) AS int),
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
            LEFT JOIN Comment c ON c.postId = p.id AND c.isDeleted = false
            LEFT JOIN Bookmark b ON b.post.id = p.id AND b.user.id = u.id
            WHERE p.boardType = :boardType
              AND (:keyword IS NULL
                   OR :keyword = ''
                   OR p.title LIKE CONCAT('%', :keyword, '%')
                   OR p.content LIKE CONCAT('%', :keyword, '%'))
            GROUP BY p.id, p.title, p.content, u.nickname, u.imageUrl,
                     u.devcourseName, u.devcourseBatch, p.boardType, ps.viewCount,
                     p.likeCount, p.createdAt, p.updatedAt, p.tags
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
                CAST(COUNT(c) AS int),
                CAST(COUNT(b) AS int),
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
            LEFT JOIN Comment c ON c.postId = p.id AND c.isDeleted = false
            LEFT JOIN Bookmark b ON b.post.id = p.id AND b.user.id = u.id
            WHERE p.boardType = :boardType
            GROUP BY p.id, p.title, p.content, u.nickname, u.imageUrl,
                     u.devcourseName, u.devcourseBatch, p.boardType, ps.viewCount,
                     p.likeCount, p.createdAt, p.updatedAt, p.tags
                                 ORDER BY ps.viewCount DESC
            """)
    Page<ITResponseDTO> findDTOByBoardTypeOrderByViewCount(@Param("boardType") BoardType boardType,
                                                           Pageable pageable);
}
