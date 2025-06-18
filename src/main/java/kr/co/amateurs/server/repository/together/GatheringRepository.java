package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringRepository extends JpaRepository<GatheringPost, Long> {
    @Query("""
      select gp
        from GatheringPost gp
        join gp.post p
       where lower(p.title)   like lower(concat('%', :keyword, '%'))
          or lower(p.content) like lower(concat('%', :keyword, '%'))
    """)
    // TODO - 쿼리에 아래 코드 추가 시 검색어가 태그에도 포함되는 지 확인 가능
    // or lower(p.tags)    like lower(concat('%', :keyword, '%'))
    Page<GatheringPost> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
