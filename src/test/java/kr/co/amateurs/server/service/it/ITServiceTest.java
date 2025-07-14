package kr.co.amateurs.server.service.it;

import jakarta.persistence.EntityManager;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.ITPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.it.ITTestFixtures;
import kr.co.amateurs.server.repository.it.ITRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ITServiceTest {
    @Autowired
    private ITService itService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ITRepository itRepository;

    @Autowired
    private PostStatisticsRepository postStatisticsRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PostEmbeddingService postEmbeddingService;

    private User testStudentUser;
    private User testOtherUser;
    private ITPost testReviewITPost;
    private ITPost testReviewITPost2;
    private ITPost testInfoITPost;

    @BeforeEach
    void setUp() throws InterruptedException {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        testStudentUser = userRepository.save(ITTestFixtures.createStudentUser());
        testOtherUser = userRepository.save(ITTestFixtures.createCustomUser("other@other.com", "other", "other", Role.STUDENT));

        Post testReviewPost = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    ITTestFixtures.createPostWithCounts(testStudentUser, "리뷰1", "리뷰1", BoardType.REVIEW, 10, 10)
            );

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });

        Thread.sleep(10);
        Post testReviewPost2 = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    ITTestFixtures.createPost(testStudentUser, "리뷰2", "리뷰2", BoardType.REVIEW)
            );

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });

        Post testInfoPost = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    ITTestFixtures.createPost(testStudentUser, "정보1", "정보2", BoardType.INFO)
            );

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });


        testReviewITPost = itRepository.save(ITTestFixtures.createITPost(testReviewPost));
        testReviewITPost2 = itRepository.save(ITTestFixtures.createITPost(testReviewPost2));
        testInfoITPost = itRepository.save(ITTestFixtures.createITPost(testInfoPost));
    }

    @Test
    void 유저가_키워드없이_검색하면_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(null)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_LATEST)
                .build();

        // when
        PageResponseDTO<ITResponseDTO> result = itService.searchPosts(boardType, param);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.pageInfo().getPageNumber()).isEqualTo(page);
        assertThat(result.pageInfo().getPageSize()).isEqualTo(pageSize);
        assertThat(result.pageInfo().getTotalPages()).isEqualTo(1);
        assertThat(result.content().get(0).title()).isEqualTo("리뷰2");
    }

    @Test
    void 유저가_키워드로_검색하면_해당_게시글목록이_반환되어야_한다() {
        // given
        String keyword = "리뷰";
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(keyword)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_LATEST)
                .build();

        // when
        PageResponseDTO<ITResponseDTO> result = itService.searchPosts(boardType, param);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).title()).contains("리뷰2");
    }

    @Test
    void 유저가_존재하지않는_키워드로_검색하면_빈_목록이_반환되어야_한다() {
        // given
        String keyword = "함께해요";
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(keyword)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_LATEST)
                .build();

        // when
        PageResponseDTO<ITResponseDTO> result = itService.searchPosts(boardType, param);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
    }

    @Test
    void 유저가_인기순으로_정렬하면_인기순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(null)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_POPULAR)
                .build();

        // when
        PageResponseDTO<ITResponseDTO> result = itService.searchPosts(boardType, param);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).likeCount()).isGreaterThanOrEqualTo(
                result.content().get(1).likeCount()
        );
        assertThat(result.content().get(0).title()).contains("리뷰1");
    }

    @Test
    void 유저가_조회순으로_정렬하면_조회순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(null)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_MOST_VIEW)
                .build();

        // when
        PageResponseDTO<ITResponseDTO> result = itService.searchPosts(boardType, param);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).viewCount()).isGreaterThanOrEqualTo(
                result.content().get(1).viewCount()
        );
        assertThat(result.content().get(0).title()).contains("리뷰1");
    }

    @Test
    void 유저가_유효한_itId로_조회하면_게시글상세가_반환되어야_한다() {
        // given
        Long itId = testReviewITPost.getId();

        // when
        ITResponseDTO result = itService.getPost(itId, "1");

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("리뷰1");
        assertThat(result.content()).isEqualTo("리뷰1");
        assertThat(result.nickname()).isEqualTo("student");
        assertThat(result.boardType()).isEqualTo(BoardType.REVIEW);
        assertThat(result.viewCount()).isEqualTo(0);
        assertThat(result.likeCount()).isEqualTo(10);
        assertThat(result.tags()).isEqualTo("테스트,태그");
        assertThat(result.hasLiked()).isFalse();
        assertThat(result.hasBookmarked()).isFalse();
    }

    @Test
    void 유저가_존재하지_않는_itId로_조회하면_예외가_발생해야_한다() {
        // given
        Long nonExistentItId = 999L;

        // when & then
        assertThatThrownBy(() -> itService.getPost(nonExistentItId, "1"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_유효한_요청으로_게시글을_생성하면_게시글이_생성되어야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("새로운 제목","태그", "새로운 내용");
        BoardType boardType = BoardType.REVIEW;

        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when
        ITResponseDTO result = itService.createPost(requestDTO, boardType);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("새로운 제목");
        assertThat(result.tags()).isEqualTo("태그");
        assertThat(result.content()).isEqualTo("새로운 내용");
        assertThat(result.boardType()).isEqualTo(BoardType.REVIEW);
        assertThat(result.nickname()).isEqualTo("student");
    }

    @Test
    void 유저가_로그인하지않은_상태로_게시글을_생성하면_예외가_발생해야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("새로운 제목", "태그","새로운 내용");
        BoardType boardType = BoardType.REVIEW;

        given(userService.getCurrentLoginUser()).willThrow(CustomException.class);

        // when & then
        assertThatThrownBy(() -> itService.createPost(requestDTO, boardType))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인_게시글을_수정하면_게시글이_수정되어야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("수정된 제목","수정 태그", "수정된 내용");
        Long itId = testReviewITPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when
        itService.updatePost(requestDTO, itId);

        // then
        ITResponseDTO result = itService.getPost(itId, "1");
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.tags()).isEqualTo("수정 태그");
        assertThat(result.content()).isEqualTo("수정된 내용");
    }

    @Test
    void 유저가_존재하지_않는_itId로_수정하면_예외가_발생해야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("수정된 제목","수정 태그","수정된 내용");
        Long nonExistentItId = 999L;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> itService.updatePost(requestDTO, nonExistentItId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_다른_게시글을_수정하면_게시글이_예외가_발생해야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("수정된 제목","수정 태그", "수정된 내용");
        Long itId = testReviewITPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testOtherUser);

        // when & then
        assertThatThrownBy(() -> itService.updatePost(requestDTO, itId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인_게시글을_삭제하면_게시글이_삭제되어야_한다() {
        // given
        Long itId = testReviewITPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when & then
        assertThatCode(() -> itService.deletePost(itId))
                .doesNotThrowAnyException();
    }

    @Test
    void 유저가_존재하지_않는_itId로_삭제하면_예외가_발생해야_한다() {
        // given
        Long nonExistentItId = 999L;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> itService.deletePost(nonExistentItId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_다른_게시글을_삭제하면_예외가_발생해야_한다() {
        // given
        Long itId = testReviewITPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testOtherUser);

        // when & then
        assertThatThrownBy(() -> itService.deletePost(itId))
                .isInstanceOf(CustomException.class);
    }
}