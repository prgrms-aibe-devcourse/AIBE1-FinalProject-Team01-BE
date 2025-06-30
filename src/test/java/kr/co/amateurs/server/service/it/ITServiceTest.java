package kr.co.amateurs.server.service.it;

import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @MockitoBean
    private UserService userService;

    private User testStudentUser;
    private User testOtherUser;
    private Post testReviewPost;
    private Post testReviewPost2;
    private Post testInfoPost;


    @BeforeEach
    void setUp() throws InterruptedException {
        testStudentUser = userRepository.save(ITTestFixtures.createStudentUser());
        testOtherUser = userRepository.save(ITTestFixtures.createCustomUser("other@other.com", "other", "other", Role.STUDENT));
        testReviewPost = postRepository.save(ITTestFixtures.createPostWithCounts(testStudentUser, "리뷰1", "리뷰1", BoardType.REVIEW, 10, 10));
        Thread.sleep(10);
        testReviewPost2 = postRepository.save(ITTestFixtures.createPost(testStudentUser, "리뷰2", "리뷰2", BoardType.REVIEW));
        testInfoPost = postRepository.save(ITTestFixtures.createPost(testStudentUser, "정보1", "정보2", BoardType.INFO));
    }

    @Test
    void 유저가_키워드없이_검색하면_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        SortType sortType = SortType.LATEST;

        // when
        Page<ITResponseDTO> result = itService.searchPosts(null, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(page);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("리뷰2");
    }

    @Test
    void 유저가_키워드로_검색하면_해당_게시글목록이_반환되어야_한다() {
        // given
        String keyword = "리뷰";
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        SortType sortType = SortType.LATEST;

        // when
        Page<ITResponseDTO> result = itService.searchPosts(keyword, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).title()).contains("리뷰2");
    }

    @Test
    void 유저가_존재하지않는_키워드로_검색하면_빈_목록이_반환되어야_한다() {
        // given
        String keyword = "함께해요";
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        SortType sortType = SortType.LATEST;

        // when
        Page<ITResponseDTO> result = itService.searchPosts(keyword, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 유저가_인기순으로_정렬하면_인기순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        SortType sortType = SortType.POPULAR;

        // when
        Page<ITResponseDTO> result = itService.searchPosts(null, page, boardType, sortType, pageSize);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).likeCount()).isGreaterThanOrEqualTo(
                result.getContent().get(1).likeCount()
        );
        assertThat(result.getContent().get(0).title()).contains("리뷰1");
    }

    @Test
    void 유저가_조회순으로_정렬하면_조회순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.REVIEW;
        SortType sortType = SortType.VIEW_COUNT;

        // when
        Page<ITResponseDTO> result = itService.searchPosts(null, page, boardType, sortType, pageSize);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).viewCount()).isGreaterThanOrEqualTo(
                result.getContent().get(1).viewCount()
        );
        assertThat(result.getContent().get(0).title()).contains("리뷰1");
    }

    @Test
    void 유저가_유효한_postId로_조회하면_게시글상세가_반환되어야_한다() {
        // given
        Long postId = testReviewPost.getId();

        // when
        ITResponseDTO result = itService.getPost(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("리뷰1");
        assertThat(result.content()).isEqualTo("리뷰1");
        assertThat(result.nickname()).isEqualTo("student");
        assertThat(result.boardType()).isEqualTo(BoardType.REVIEW);
        assertThat(result.viewCount()).isEqualTo(10);
        assertThat(result.likeCount()).isEqualTo(10);
        assertThat(result.tags()).isEqualTo("테스트,태그");
        assertThat(result.hasLiked()).isFalse();
        assertThat(result.hasBookmarked()).isFalse();
    }

    @Test
    void 유저가_존재하지_않는_postId로_조회하면_예외가_발생해야_한다() {
        // given
        Long nonExistentPostId = 999L;

        // when & then
        assertThatThrownBy(() -> itService.getPost(nonExistentPostId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_유효한_요청으로_게시글을_생성하면_게시글이_생성되어야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("새로운 제목","태그", "새로운 내용");
        BoardType boardType = BoardType.REVIEW;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

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

        given(userService.getCurrentUser()).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itService.createPost(requestDTO, boardType))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인_게시글을_수정하면_게시글이_수정되어야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("수정된 제목","수정 태그", "수정된 내용");
        Long postId = testReviewPost.getId();

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when
        itService.updatePost(requestDTO, postId);

        // then
        ITResponseDTO result = itService.getPost(postId);
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.tags()).isEqualTo("수정 태그");
        assertThat(result.content()).isEqualTo("수정된 내용");
    }

    @Test
    void 유저가_존재하지_않는_postId로_수정하면_예외가_발생해야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("수정된 제목","수정 태그","수정된 내용");
        Long nonExistentPostId = 999L;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> itService.updatePost(requestDTO, nonExistentPostId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_다른_게시글을_수정하면_게시글이_예외가_발생해야_한다() {
        // given
        ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO("수정된 제목","수정 태그", "수정된 내용");
        Long postId = testReviewPost.getId();

        given(userService.getCurrentUser()).willReturn(Optional.of(testOtherUser));

        // when & then
        assertThatThrownBy(() -> itService.updatePost(requestDTO, postId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인_게시글을_삭제하면_게시글이_삭제되어야_한다() {
        // given
        Long postId = testReviewPost.getId();

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when
        itService.deletePost(postId);

        // then
        assertThatThrownBy(() -> itService.getPost(postId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_존재하지_않는_postId로_삭제하면_예외가_발생해야_한다() {
        // given
        Long nonExistentPostId = 999L;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> itService.deletePost(nonExistentPostId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_다른_게시글을_삭제하면_예외가_발생해야_한다() {
        // given
        Long postId = testReviewPost.getId();

        given(userService.getCurrentUser()).willReturn(Optional.of(testOtherUser));

        // when & then
        assertThatThrownBy(() -> itService.deletePost(postId))
                .isInstanceOf(CustomException.class);
    }
}