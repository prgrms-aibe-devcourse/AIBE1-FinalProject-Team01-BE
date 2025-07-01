package kr.co.amateurs.server.repository.community;

import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface CommunityRepository extends JpaRepository<CommunityPost, Long> {
    @Query("SELECT cp FROM CommunityPost cp JOIN cp.post p WHERE p.boardType = :boardType")
    Page<CommunityPost> findByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);

    @Query("""
            select cp
            from CommunityPost cp
            join cp.post p
            where p.boardType = :boardType
              and (:keyword is null
                   or :keyword = ''
                   or p.title like concat('%', :keyword, '%')
                   or p.content like concat('%', :keyword, '%'))
            """)
    Page<CommunityPost> findByContentAndBoardType(@Param("keyword") String keyword,
                                                  @Param("boardType") BoardType boardType,
                                                  Pageable pageable);
}
