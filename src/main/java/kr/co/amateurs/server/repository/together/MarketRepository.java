package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.MarketItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<MarketItem, Long> {
}
