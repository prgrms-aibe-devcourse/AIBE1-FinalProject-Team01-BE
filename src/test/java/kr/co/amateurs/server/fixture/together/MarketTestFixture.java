package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;

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

    public static Post createJavaPost(User user) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.MARKET)
                .title("Java 책")
                .content("Java 책 중고로 팝니다.")
                .tags("책,자바")
                .build();
    }

    public static Post createPythonPost(User user) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.MARKET)
                .title("Python 책")
                .content("Python 책 중고로 팝니다.")
                .tags("책,파이썬")
                .build();
    }

    public static MarketItem createJavaMarketItem(Post post) {
        return MarketItem.builder()
                .post(post)
                .status(MarketStatus.SELLING)
                .price(10000)
                .place("서울")
                .build();
    }

    public static MarketItem createPythonMarketItem(Post post) {
        return MarketItem.builder()
                .post(post)
                .status(MarketStatus.SELLING)
                .price(15000)
                .place("서울")
                .build();
    }
}