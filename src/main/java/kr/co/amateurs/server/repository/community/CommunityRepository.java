package kr.co.amateurs.server.repository.community;

import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface CommunityRepository extends JpaRepository<CommunityPost, Long> {
    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            cp.id,
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
        FROM CommunityPost cp
        JOIN cp.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
        """)
    Page<CommunityResponseDTO> findDTOByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            cp.id,
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
        FROM CommunityPost cp
        JOIN cp.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        """)
    Page<CommunityResponseDTO> findDTOByContentAndBoardType(@Param("keyword") String keyword,
                                                            @Param("boardType") BoardType boardType,
                                                            Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            cp.id, p.id, p.title, p.content, u.nickname, u.imageUrl, 
            u.devcourseName, u.devcourseBatch, p.boardType, p.isBlinded, ps.viewCount, 
            p.likeCount, 
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id),
            p.createdAt, p.updatedAt, p.tags,
            (SELECT CASE WHEN COUNT(pl2.id) > 0 THEN true ELSE false END FROM Like pl2 WHERE pl2.post.id = p.id AND pl2.user.id = :userId),
            (SELECT CASE WHEN COUNT(b3.id) > 0 THEN true ELSE false END FROM Bookmark b3 WHERE b3.post.id = p.id AND b3.user.id = :userId)
        )
        FROM CommunityPost cp
        JOIN cp.post p JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE cp.id = :communityId
        """)
    Optional<CommunityResponseDTO> findDTOByIdForUser(@Param("communityId") Long communityId,
                                                      @Param("userId") Long userId);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            cp.id,
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
        FROM CommunityPost cp
        JOIN cp.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
        ORDER BY ps.viewCount DESC
        """)
    Page<CommunityResponseDTO> findDTOByBoardTypeOrderByViewCount(@Param("boardType") BoardType boardType,
                                                                  Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            cp.id,
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
        FROM CommunityPost cp
        JOIN cp.post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        ORDER BY ps.viewCount DESC
        """)
    Page<CommunityResponseDTO> findDTOByContentAndBoardTypeOrderByViewCount(@Param("keyword") String keyword,
                                                                            @Param("boardType") BoardType boardType,
                                                                            Pageable pageable);
}