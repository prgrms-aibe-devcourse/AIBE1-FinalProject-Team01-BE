package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
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
public class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    void 모든_모집글_조회시_전체목록_반환() {
        // given
        User user = createAndSaveUser();
        createAndSaveMatchingPost(user, "Java 커피챗", "Java 커피챗 모집합니다");
        createAndSaveMatchingPost(user, "React 멘토링", "React 멘토링 멘티 구합니다");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<MatchingPost> result = matchRepository.findAll(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 제목에_키워드가_포함된_모집글_검색시_해당글만_반환() {
        // given
        User user = createAndSaveUser();
        createAndSaveMatchingPost(user, "Java 커피챗", "Java와 관련된 커피챗");
        createAndSaveMatchingPost(user, "Python 커피챗", "Python와 관련된 커피챗");
        createAndSaveMatchingPost(user, "React 멘토링", "React를 배우는 멘토링");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Java";

        // when
        Page<MatchingPost> result = matchRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPost().getTitle()).contains("Java");
    }

    @Test
    void 내용에_키워드가_포함된_모집글_검색시_해당글만_반환() {
        // given
        User user = createAndSaveUser();
        createAndSaveMatchingPost(user, "웹 개발 커피챗", "Spring Boot를 이용한 백엔드 개발 이야기");
        createAndSaveMatchingPost(user, "앱 개발 멘토링", "React Native로 모바일 앱 개발");
        createAndSaveMatchingPost(user, "데이터 분석", "Python을 이용한 데이터 분석 커피챗");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Spring";

        // when
        Page<MatchingPost> result = matchRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPost().getContent()).contains("Spring");
    }

    @Test
    void 제목이나_내용에_키워드가_포함된_모집글_검색시_모든해당글_반환() {
        // given
        User user = createAndSaveUser();
        createAndSaveMatchingPost(user, "React 커피챗", "React 이야기 커피챗");
        createAndSaveMatchingPost(user, "프론트엔드 멘토링", "React를 이용한 프론트엔드 개발");
        createAndSaveMatchingPost(user, "백엔드 커피챗", "Spring Boot 백엔드 개발");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "React";

        // when
        Page<MatchingPost> result = matchRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(gp ->
                gp.getPost().getTitle().contains("React") || gp.getPost().getContent().contains("React"));
    }


    @Test
    void 존재하지않는_키워드로_검색시_빈결과_반환() {
        // given
        User user = createAndSaveUser();
        createAndSaveMatchingPost(user, "Java 커피챗", "Java 개발자 커피챗");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "존재하지않는키워드";

        // when
        Page<MatchingPost> result = matchRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void 공백이_포함된_키워드로_검색시_정상작동() {
        // given
        User user = createAndSaveUser();
        createAndSaveMatchingPost(user, "Spring Boot 멘토링", "Spring Boot로 웹 개발");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Spring Boot";

        // when
        Page<MatchingPost> result = matchRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void 페이징_처리가_정상적으로_작동() {
        // given
        User user = createAndSaveUser();
        for (int i = 1; i <= 15; i++) {
            createAndSaveMatchingPost(user, "커피챗 " + i, "커피챗 내용 " + i);
        }

        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        // when
        Page<MatchingPost> firstResult = matchRepository.findAll(firstPage);
        Page<MatchingPost> secondResult = matchRepository.findAll(secondPage);

        // then
        assertThat(firstResult.getContent()).hasSize(5);
        assertThat(secondResult.getContent()).hasSize(5);
        assertThat(firstResult.getTotalElements()).isEqualTo(15);
        assertThat(firstResult.getTotalPages()).isEqualTo(3);
    }

    @Test
    void ID로_모집글_조회시_해당글_반환() {
        // given
        User user = createAndSaveUser();
        MatchingPost savedPost = createAndSaveMatchingPost(user, "테스트 모집", "테스트 내용");

        // when
        MatchingPost result = matchRepository.findById(savedPost.getId()).orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPost().getTitle()).isEqualTo("테스트 모집");
        assertThat(result.getMatchingType()).isEqualTo(MatchingType.COFFEE_CHAT);
    }

    @Test
    void 존재하지않는_ID로_조회시_빈결과_반환() {
        // when
        boolean exists = matchRepository.findById(999L).isPresent();

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 모집글_저장시_정상적으로_저장됨() {
        // given
        User user = createAndSaveUser();
        Post post = createAndSavePost(user, "새 모집", "새 모집 내용");

        MatchingPost matchingPost = MatchingPost.builder()
                .post(post)
                .matchingType(MatchingType.COFFEE_CHAT)
                .status(MatchingStatus.OPEN)
                .expertiseAreas("부산")
                .build();

        // when
        MatchingPost saved = matchRepository.save(matchingPost);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMatchingType()).isEqualTo(MatchingType.COFFEE_CHAT);
        assertThat(saved.getStatus()).isEqualTo(MatchingStatus.OPEN);
        assertThat(saved.getExpertiseAreas()).isEqualTo("부산");
    }

    @Test
    void 모집글_삭제시_정상적으로_삭제됨() {
        // given
        User user = createAndSaveUser();
        MatchingPost savedPost = createAndSaveMatchingPost(user, "삭제할 모집", "삭제할 내용");
        Long postId = savedPost.getId();

        // when
        matchRepository.deleteById(postId);

        // then
        boolean exists = matchRepository.findById(postId).isPresent();
        assertThat(exists).isFalse();
    }


    private User createAndSaveUser() {
        User user = User.builder()
                .email("test@email.com")
                .password("password")
                .nickname("testUser")
                .name("이름")
                .role(Role.STUDENT)
                .build();
        return userRepository.save(user);
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

    private MatchingPost createAndSaveMatchingPost(User user, String title, String content) {
        Post post = createAndSavePost(user, title, content);

        MatchingPost matchingPost = MatchingPost.builder()
                .post(post)
                .matchingType(MatchingType.COFFEE_CHAT)
                .status(MatchingStatus.OPEN)
                .expertiseAreas("서울")
                .build();

        return matchRepository.save(matchingPost);
    }
}