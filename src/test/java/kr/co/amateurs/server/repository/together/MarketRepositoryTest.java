package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MarketRepositoryTest {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@email.com")
                .password("password")
                .nickname("testUser")
                .name("이름")
                .role(Role.STUDENT)
                .build();
        user = userRepository.save(user);
    }

    @Test
    void 모든_장터글_조회시_전체목록_반환() {
        // given
        
        createAndSaveMarketPost(user, "Java 책", "Java 책 팝니다");
        createAndSaveMarketPost(user, "React 책", "React 책 팝니다");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<MarketItem> result = marketRepository.findAllByKeyword(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 제목에_키워드가_포함된_장터글_검색시_해당글만_반환() {
        // given
        
        createAndSaveMarketPost(user, "Java 책", "Java 언어를 배우는 책");
        createAndSaveMarketPost(user, "Python 책", "Python 언어를 배우는 책");
        createAndSaveMarketPost(user, "React 책", "React 책입니다");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Java";

        // when
        Page<MarketItem> result = marketRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPost().getTitle()).contains("Java");
    }

    @Test
    void 내용에_키워드가_포함된_장터글_검색시_해당글만_반환() {
        // given
        
        createAndSaveMarketPost(user, "웹 개발 책", "Spring Boot를 이용한 백엔드 개발");
        createAndSaveMarketPost(user, "앱 개발 책", "React Native로 모바일 앱 개발");
        createAndSaveMarketPost(user, "데이터 분석", "Python을 이용한 데이터 분석 책");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Spring";

        // when
        Page<MarketItem> result = marketRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPost().getContent()).contains("Spring");
    }

    @Test
    void 제목이나_내용에_키워드가_포함된_장터글_검색시_모든해당글_반환() {
        // given
        
        createAndSaveMarketPost(user, "React 책", "React 기초부터 배우는 책");
        createAndSaveMarketPost(user, "프론트엔드 책", "React를 이용한 프론트엔드 개발");
        createAndSaveMarketPost(user, "백엔드 책", "Spring Boot 백엔드 개발");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "React";

        // when
        Page<MarketItem> result = marketRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(gp ->
                gp.getPost().getTitle().contains("React") || gp.getPost().getContent().contains("React"));
    }


    @Test
    void 존재하지않는_키워드로_검색시_빈결과_반환() {
        // given
        
        createAndSaveMarketPost(user, "Java 책", "Java 언어 책");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "존재하지않는키워드";

        // when
        Page<MarketItem> result = marketRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void 공백이_포함된_키워드로_검색시_정상작동() {
        // given
        
        createAndSaveMarketPost(user, "Spring Boot 책", "Spring Boot로 웹 개발");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Spring Boot";

        // when
        Page<MarketItem> result = marketRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void 페이징_처리가_정상적으로_작동() {
        // given
        
        for (int i = 1; i <= 15; i++) {
            createAndSaveMarketPost(user, "책 " + i, "책 내용 " + i);
        }

        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        // when
        Page<MarketItem> firstResult = marketRepository.findAllByKeyword(null, firstPage);
        Page<MarketItem> secondResult = marketRepository.findAllByKeyword(null, secondPage);

        // then
        assertThat(firstResult.getContent()).hasSize(5);
        assertThat(secondResult.getContent()).hasSize(5);
        assertThat(firstResult.getTotalElements()).isEqualTo(15);
        assertThat(firstResult.getTotalPages()).isEqualTo(3);
    }

    @Test
    void ID로_장터글_조회시_해당글_반환() {
        // given
        
        MarketItem savedPost = createAndSaveMarketPost(user, "테스트 장터", "테스트 내용");

        // when
        MarketItem result = marketRepository.findById(savedPost.getId()).orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPost().getTitle()).isEqualTo("테스트 장터");
    }

    @Test
    void 존재하지않는_ID로_조회시_빈결과_반환() {
        // when
        boolean exists = marketRepository.findById(999L).isPresent();

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 장터글_저장시_정상적으로_저장됨() {
        // given
        
        Post post = createAndSavePost(user, "새 장터", "새 장터 내용");

        MarketItem marketPost = MarketItem.builder()
                .post(post)
                .status(MarketStatus.SELLING)
                .price(1000)
                .place("부산")
                .build();

        // when
        MarketItem saved = marketRepository.save(marketPost);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(MarketStatus.SELLING);
        assertThat(saved.getPrice()).isEqualTo(1000);
        assertThat(saved.getPlace()).isEqualTo("부산");
    }

    @Test
    void 장터글_삭제시_정상적으로_삭제됨() {
        // given
        
        MarketItem savedPost = createAndSaveMarketPost(user, "삭제할 장터", "삭제할 내용");
        Long postId = savedPost.getId();

        // when
        marketRepository.deleteById(postId);

        // then
        boolean exists = marketRepository.findById(postId).isPresent();
        assertThat(exists).isFalse();
    }
    
    private Post createAndSavePost(User user, String title, String content) {
        Post post = Post.builder()
                .user(user)
                .boardType(BoardType.GATHER)
                .title(title)
                .content(content)
                .tags("태그")
                .build();
        return postRepository.save(post);
    }

    private MarketItem createAndSaveMarketPost(User user, String title, String content) {
        Post post = createAndSavePost(user, title, content);

        MarketItem marketPost = MarketItem.builder()
                .post(post)
                .status(MarketStatus.SELLING)
                .price(1000)
                .place("서울")
                .build();

        return marketRepository.save(marketPost);
    }
}