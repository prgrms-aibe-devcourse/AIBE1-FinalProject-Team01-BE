package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;

import java.time.LocalDateTime;

public class MarketTestFixture {
    public static MarketPostRequestDTO createMarketPostRequestDTO() {
        return new MarketPostRequestDTO(
                "Java 책",
                "Java 책 중고로 팝니다.",
                "책",
                MarketStatus.SELLING,
                10000,
                "서울"
        );
    }

}