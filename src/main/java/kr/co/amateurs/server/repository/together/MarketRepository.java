package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface MarketRepository extends JpaRepository<MarketItem, Long> {
    // TODO - 쿼리에 아래 코드 추가 시 검색어가 태그에도 포함되는 지 확인 가능
    // or p.tags    like concat('%', :keyword, '%')
    @Query("""
        select g
        from MarketItem g
        join fetch g.post p
        where (:keyword is null
               or :keyword = ''
               or p.title   like concat('%', :keyword, '%')
               or p.content like concat('%', :keyword, '%')
             )
    """)
    Page<MarketItem> findAllByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    MarketItem findByPostId(Long postId);
}
