package kr.co.amateurs.server.repository.it;

import kr.co.amateurs.server.domain.entity.post.ITPost;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface ITRepository extends JpaRepository<ITPost, Long> {
    @Query("SELECT ip FROM ITPost ip JOIN ip.post p WHERE p.boardType = :boardType")
    Page<ITPost> findByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);

    @Query("""
        select ip
        from ITPost ip
        join fetch ip.post p
        where p.boardType = :boardType
          and (:keyword is null
               or :keyword = ''
               or p.title like concat('%', :keyword, '%')
               or p.content like concat('%', :keyword, '%'))
        """)
    Page<ITPost> findByContentAndBoardType(@Param("keyword") String keyword,
                                           @Param("boardType") BoardType boardType,
                                           Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = (SELECT ip.post.id FROM ITPost ip WHERE ip.id = :itId)")
    void increaseViewCount(@Param("itId") Long itId);
}
