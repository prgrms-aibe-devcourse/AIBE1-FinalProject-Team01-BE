package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.it.ITTestFixtures;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
@Transactional
class ViewCountServiceTest {

    @Autowired
    private ViewCountService viewCountService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PostStatisticsRepository postStatisticsRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String testIp1 = "192.168.1.1";
    private String testIp2 = "192.168.1.2";
    private String testIp3 = "10.0.0.1";
    Post post;
    Post post2;
    User studentUser;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        cleanUpData();
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void 같은_IP로_24시간_내_중복_조회_시_조회수가_한_번만_증가해야_한다() {
        // given & when
        boolean firstAttempt = viewCountService.incrementViewCount(post.getId(), testIp1);
        boolean secondAttempt = viewCountService.incrementViewCount(post.getId(), testIp1);
        boolean thirdAttempt = viewCountService.incrementViewCount(post.getId(), testIp1);

        // then
        assertThat(firstAttempt).isTrue();
        assertThat(secondAttempt).isFalse();
        assertThat(thirdAttempt).isFalse();
        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(1L);
    }

    @Test
    void 다른_IP들로_조회_시_각각_조회수가_증가해야_한다() {
        // given & when
        boolean ip1Result = viewCountService.incrementViewCount(post.getId(), testIp1);
        boolean ip2Result = viewCountService.incrementViewCount(post.getId(), testIp2);
        boolean ip3Result = viewCountService.incrementViewCount(post.getId(), testIp3);

        // then
        assertThat(ip1Result).isTrue();
        assertThat(ip2Result).isTrue();
        assertThat(ip3Result).isTrue();
        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(3L);
    }

    @Test
    void Redis에만_있는_조회수와_DB_기존_조회수를_합쳐서_반환해야_한다() {
        // given
        viewCountService.incrementViewCount(post.getId(), testIp1);
        viewCountService.incrementViewCount(post.getId(), testIp2);

        // when
        Long totalViewCount = viewCountService.getViewCount(post.getId());

        // then
        assertThat(totalViewCount).isEqualTo(2L);

        Optional<PostStatistics> stats = postStatisticsRepository.findById(post.getId());
        assertThat(stats.get().getViewCount()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_게시글의_조회수는_Redis_값만_반환해야_한다() {
        // given
        Long nonExistentPostId = 999L;
        viewCountService.incrementViewCount(nonExistentPostId, testIp1);

        // when
        Long viewCount = viewCountService.getViewCount(nonExistentPostId);

        // then
        assertThat(viewCount).isEqualTo(1L);
    }

    @Test
    void Redis_조회수_데이터를_DB에_동기화하고_Redis를_정리해야_한다() {
        // given
        viewCountService.incrementViewCount(post.getId(), testIp1);
        viewCountService.incrementViewCount(post.getId(), testIp2);
        viewCountService.incrementViewCount(post.getId(), testIp3);

        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(3L);
        Optional<PostStatistics> beforeSync = postStatisticsRepository.findById(post.getId());
        assertThat(beforeSync.get().getViewCount()).isEqualTo(0);

        // when
        viewCountService.syncViewCountsToDB();

        // then
        Optional<PostStatistics> afterSync = postStatisticsRepository.findById(post.getId());
        assertThat(afterSync.get().getViewCount()).isEqualTo(3);

        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(3L);

        boolean newIncrement = viewCountService.incrementViewCount(post.getId(), testIp1);
        assertThat(newIncrement).isFalse();
        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(3L);
    }

    @Test
    void 여러_게시글의_조회수를_동시에_동기화해야_한다() {
        // given

        viewCountService.incrementViewCount(post.getId(), testIp1);
        viewCountService.incrementViewCount(post.getId(), testIp2);
        viewCountService.incrementViewCount(post2.getId(), testIp1);
        viewCountService.incrementViewCount(post2.getId(), testIp3);

        // when
        viewCountService.syncViewCountsToDB();

        // then
        Optional<PostStatistics> post1Stats = postStatisticsRepository.findById(post.getId());
        Optional<PostStatistics> post2Stats = postStatisticsRepository.findById(post2.getId());

        assertThat(post1Stats.get().getViewCount()).isEqualTo(2);
        assertThat(post2Stats.get().getViewCount()).isEqualTo(2);
    }

    @Test
    void 특정_게시글의_캐시를_초기화하면_해당_게시글의_Redis_데이터만_삭제되어야_한다() {
        // given
        viewCountService.incrementViewCount(post.getId(), testIp1);
        viewCountService.incrementViewCount(post.getId(), testIp2);
        viewCountService.incrementViewCount(post2.getId(), testIp1);

        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(2L);
        assertThat(viewCountService.getViewCount(post2.getId())).isEqualTo(1L);

        // when
        viewCountService.clearViewCountCache(post.getId());

        // then
        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(0L);
        assertThat(viewCountService.getViewCount(post2.getId())).isEqualTo(1L);

        boolean canIncreaseAgain = viewCountService.incrementViewCount(post.getId(), testIp1);
        assertThat(canIncreaseAgain).isTrue();
    }

    @Test
    void 동기화_중_일부_데이터_오류가_있어도_다른_데이터는_정상_처리되어야_한다() {
        // given
        Long invalidPostId = 999L;

        viewCountService.incrementViewCount(post.getId(), testIp1);
        viewCountService.incrementViewCount(invalidPostId, testIp1);

        // when
        viewCountService.syncViewCountsToDB();

        // then
        Optional<PostStatistics> validStats = postStatisticsRepository.findById(post.getId());
        assertThat(validStats.get().getViewCount()).isEqualTo(1);

        assertThat(viewCountService.getViewCount(post.getId())).isEqualTo(1L);
        assertThat(viewCountService.getViewCount(invalidPostId)).isEqualTo(0L);
    }

    private void setupTestData() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        studentUser = userRepository.save(ITTestFixtures.createStudentUser());

        post = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    ITTestFixtures.createPost(studentUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.REVIEW));

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });

        post2 = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    ITTestFixtures.createPost(studentUser, "테스트 게시글 제목2", "테스트 게시글 내용2", BoardType.REVIEW));

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });
    }

    private void cleanUpData() {
        postStatisticsRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}
