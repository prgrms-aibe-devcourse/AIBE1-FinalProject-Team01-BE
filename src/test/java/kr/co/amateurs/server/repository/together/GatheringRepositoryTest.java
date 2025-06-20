package kr.co.amateurs.server.repository.together;

import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
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
public class GatheringRepositoryTest {

    @Autowired
    private GatheringRepository gatheringRepository;

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
    void 모든_모집글_조회시_전체목록_반환() {
        // given
        createAndSaveGatheringPost(user, "Java 스터디", "Java 스터디 모집합니다");
        createAndSaveGatheringPost(user, "React 프로젝트", "React 프로젝트 팀원 구합니다");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<GatheringPost> result = gatheringRepository.findAllByKeyword(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 제목에_키워드가_포함된_모집글_검색시_해당글만_반환() {
        // given
        createAndSaveGatheringPost(user, "Java 스터디", "Java 언어를 배우는 스터디");
        createAndSaveGatheringPost(user, "Python 스터디", "Python 언어를 배우는 스터디");
        createAndSaveGatheringPost(user, "React 프로젝트", "React로 프로젝트를 만드는 모집");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Java";

        // when
        Page<GatheringPost> result = gatheringRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPost().getTitle()).contains("Java");
    }

    @Test
    void 내용에_키워드가_포함된_모집글_검색시_해당글만_반환() {
        // given
        createAndSaveGatheringPost(user, "웹 개발 스터디", "Spring Boot를 이용한 백엔드 개발");
        createAndSaveGatheringPost(user, "앱 개발 프로젝트", "React Native로 모바일 앱 개발");
        createAndSaveGatheringPost(user, "데이터 분석", "Python을 이용한 데이터 분석 스터디");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Spring";

        // when
        Page<GatheringPost> result = gatheringRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPost().getContent()).contains("Spring");
    }

    @Test
    void 제목이나_내용에_키워드가_포함된_모집글_검색시_모든해당글_반환() {
        // given
        createAndSaveGatheringPost(user, "React 스터디", "React 기초부터 배우는 스터디");
        createAndSaveGatheringPost(user, "프론트엔드 프로젝트", "React를 이용한 프론트엔드 개발");
        createAndSaveGatheringPost(user, "백엔드 스터디", "Spring Boot 백엔드 개발");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "React";

        // when
        Page<GatheringPost> result = gatheringRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(gp ->
                gp.getPost().getTitle().contains("React") || gp.getPost().getContent().contains("React"));
    }

    @Test
    void 존재하지않는_키워드로_검색시_빈결과_반환() {
        // given
        createAndSaveGatheringPost(user, "Java 스터디", "Java 언어 스터디");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "존재하지않는키워드";

        // when
        Page<GatheringPost> result = gatheringRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void 공백이_포함된_키워드로_검색시_정상작동() {
        // given
        createAndSaveGatheringPost(user, "Spring Boot 스터디", "Spring Boot로 웹 개발");

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "Spring Boot";

        // when
        Page<GatheringPost> result = gatheringRepository.findAllByKeyword(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void 페이징_처리가_정상적으로_작동() {
        // given
        for (int i = 1; i <= 15; i++) {
            createAndSaveGatheringPost(user, "스터디 " + i, "스터디 내용 " + i);
        }

        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        // when
        Page<GatheringPost> firstResult = gatheringRepository.findAllByKeyword(null, firstPage);
        Page<GatheringPost> secondResult = gatheringRepository.findAllByKeyword(null, secondPage);

        // then
        assertThat(firstResult.getContent()).hasSize(5);
        assertThat(secondResult.getContent()).hasSize(5);
        assertThat(firstResult.getTotalElements()).isEqualTo(15);
        assertThat(firstResult.getTotalPages()).isEqualTo(3);
    }

    @Test
    void 모집글_생성일자_내림차순으로_정렬되는지_검증() throws InterruptedException {
        // given

        GatheringPost first = createAndSaveGatheringPost(user, "오래된 모집", "내용1");
        Thread.sleep(10);
        GatheringPost second = createAndSaveGatheringPost(user, "최근 모집", "내용2");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        //when
        Page<GatheringPost> result = gatheringRepository.findAllByKeyword("모집", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getPost().getTitle()).isEqualTo("최근 모집");
        assertThat(result.getContent().get(1).getPost().getTitle()).isEqualTo("오래된 모집");
    }

    @Test
    void ID로_모집글_조회시_해당글_반환() {
        // given
        GatheringPost savedPost = createAndSaveGatheringPost(user, "테스트 모집", "테스트 내용");

        // when
        GatheringPost result = gatheringRepository.findById(savedPost.getId()).orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPost().getTitle()).isEqualTo("테스트 모집");
        assertThat(result.getGatheringType()).isEqualTo(GatheringType.STUDY);
    }

    @Test
    void 존재하지않는_ID로_조회시_빈결과_반환() {
        // when
        boolean exists = gatheringRepository.findById(999L).isPresent();

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 모집글_저장시_정상적으로_저장됨() {
        // given
        Post post = createAndSavePost(user, "새 모집", "새 모집 내용");

        GatheringPost gatheringPost = GatheringPost.builder()
                .post(post)
                .gatheringType(GatheringType.STUDY)
                .status(GatheringStatus.RECRUITING)
                .headCount(10)
                .place("부산")
                .period("2개월")
                .schedule("주 3회")
                .build();

        // when
        GatheringPost saved = gatheringRepository.save(gatheringPost);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGatheringType()).isEqualTo(GatheringType.STUDY);
        assertThat(saved.getHeadCount()).isEqualTo(10);
        assertThat(saved.getPlace()).isEqualTo("부산");
    }

    @Test
    void 모집글_삭제시_정상적으로_삭제됨() {
        // given
        GatheringPost savedPost = createAndSaveGatheringPost(user, "삭제할 모집", "삭제할 내용");
        Long postId = savedPost.getId();

        // when
        gatheringRepository.deleteById(postId);

        // then
        boolean exists = gatheringRepository.findById(postId).isPresent();
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

    private GatheringPost createAndSaveGatheringPost(User user, String title, String content) {
        Post post = createAndSavePost(user, title, content);

        GatheringPost gatheringPost = GatheringPost.builder()
                .post(post)
                .gatheringType(GatheringType.STUDY)
                .status(GatheringStatus.RECRUITING)
                .headCount(5)
                .place("서울")
                .period("1개월")
                .schedule("주 2회")
                .build();

        return gatheringRepository.save(gatheringPost);
    }
}