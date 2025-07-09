package kr.co.amateurs.server.service.together;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.like.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static kr.co.amateurs.server.domain.entity.post.Post.convertTagToList;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.createAdmin;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.createStudent;
import static kr.co.amateurs.server.fixture.together.MarketTestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MarketServiceTest {

    @Autowired
    private MarketService marketService;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LikeService likeService;

    private User sellerUser;
    private User otherUser;
    private User adminUser;
    private Post javaPost;
    private Post pythonPost;
    private MarketItem javaMarketItem;
    private MarketItem pythonMarketItem;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        commentRepository.deleteAll();
        marketRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        sellerUser = createStudent();
        otherUser = createStudent();
        adminUser = createAdmin();
        sellerUser = userRepository.save(sellerUser);
        otherUser = userRepository.save(otherUser);
        adminUser = userRepository.save(adminUser);

        javaPost = createJavaPost(sellerUser);
        pythonPost = createPythonPost(sellerUser);

        javaPost = postRepository.save(javaPost);
        pythonPost = postRepository.save(pythonPost);

        javaMarketItem = createJavaMarketItem(javaPost);
        pythonMarketItem = createPythonMarketItem(pythonPost);

        javaMarketItem = marketRepository.save(javaMarketItem);
        pythonMarketItem = marketRepository.save(pythonMarketItem);

        given(likeService.checkHasLiked(javaPost.getId(), sellerUser.getId())).willReturn(false);
        given(likeService.checkHasLiked(pythonPost.getId(), sellerUser.getId())).willReturn(false);
    }

    @Nested
    class 장터_게시글_목록_조회를 {
        @Test
        void 키워드없이_조회하면_모든_장터_게시글이_반환되어야_한다() {
            PostPaginationParam paginationParam = PostPaginationParam.builder()
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MarketPostResponseDTO> result = marketService.getMarketPostList(paginationParam);

            assertThat(result.content()).hasSize(2);
            assertThat(result.pageInfo().getTotalElements()).isEqualTo(2);
            assertThat(result.pageInfo().getPageNumber()).isEqualTo(0);
        }

        @Test
        void 제목으로_조회하면_키워드일치_게시글반환() {
            PostPaginationParam param = PostPaginationParam.builder()
                    .keyword("Java")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MarketPostResponseDTO> result = marketService.getMarketPostList(param);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).title()).contains("Java");
        }

        @Test
        void 내용으로_조회하면_키워드일치_게시글반환() {
            PostPaginationParam param = PostPaginationParam.builder()
                    .keyword("Python")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MarketPostResponseDTO> result = marketService.getMarketPostList(param);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).content()).contains("Python");
        }

        @Test
        void 빈_키워드로_조회하면_모든_게시글이_반환되어야_한다() {
            PostPaginationParam param = PostPaginationParam.builder()
                    .keyword("")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MarketPostResponseDTO> result = marketService.getMarketPostList(param);

            assertThat(result.content()).hasSize(2);
        }
    }

    @Nested
    class 장터_게시글_상세_조회를 {
        @Test
        void 존재하는_ID로_조회하면_해당_장터_게시글이_반환되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(sellerUser);

            MarketPostResponseDTO dto = marketService.getMarketPost(javaMarketItem.getId());

            assertThat(dto.postId()).isEqualTo(javaPost.getId());
            assertThat(dto.title()).isEqualTo("Java 책");
            assertThat(dto.status()).isEqualTo(MarketStatus.SELLING);
            assertThat(dto.price()).isEqualTo(10000);
            assertThat(dto.place()).isEqualTo("서울");
        }

        @Test
        void 존재하지_않는_ID로_조회하면_예외가_발생해야_한다() {
            assertThatThrownBy(() -> marketService.getMarketPost(999L))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 장터_게시글_생성을 {
        @Test
        void 유효한_데이터로_생성하면_새로운_장터_게시글이_생성되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(sellerUser);
            MarketPostRequestDTO dto = createMarketPostRequestDTO();

            MarketPostResponseDTO result = marketService.createMarketPost(dto);

            assertThat(result.title()).isEqualTo("Java 책");
            assertThat(result.content()).isEqualTo("Java 책 중고로 팝니다.");
            assertThat(result.status()).isEqualTo(MarketStatus.SELLING);
            assertThat(result.price()).isEqualTo(10000);
            assertThat(result.place()).isEqualTo("서울");
            assertThat(marketRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    class 장터_게시글_수정을 {
        @Test
        void 작성자가_수정하면_게시글이_수정되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(sellerUser);
            MarketPostRequestDTO updateDto = new MarketPostRequestDTO(
                    "Java 책 판매완료",
                    "Java 책이 판매되었습니다.",
                    convertTagToList("책,자바"),
                    MarketStatus.SOLD_OUT,
                    10000,
                    "서울"
            );

            marketService.updateMarketPost(javaMarketItem.getId(), updateDto);

            MarketItem updated = marketRepository.findById(javaMarketItem.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MarketStatus.SOLD_OUT);
            assertThat(updated.getPrice()).isEqualTo(10000);
            assertThat(updated.getPlace()).isEqualTo("서울");
        }

        @Test
        void 작성자가_아닌_사용자가_수정하면_예외가_발생해야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(otherUser);
            MarketPostRequestDTO updateDto = new MarketPostRequestDTO(
                    "Java 책 판매완료",
                    "Java 책이 판매되었습니다.",
                    convertTagToList("책,자바"),
                    MarketStatus.SOLD_OUT,
                    10000,
                    "서울"
            );

            assertThatThrownBy(() -> marketService.updateMarketPost(javaMarketItem.getId(), updateDto))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_게시글을_수정하면_예외가_발생해야_한다() {
            MarketPostRequestDTO updateDto = new MarketPostRequestDTO(
                    "title",
                    "content",
                    convertTagToList("tags"),
                    MarketStatus.SELLING,
                    5000,
                    "부산"
            );
            given(userService.getCurrentLoginUser()).willReturn(sellerUser);

            assertThatThrownBy(() -> marketService.updateMarketPost(999L, updateDto))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 장터_게시글_삭제를 {
        @Test
        void 작성자가_삭제하면_게시글이_삭제되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(sellerUser);

            marketService.deleteMarketPost(javaMarketItem.getId());

            assertThat(postRepository.findById(javaPost.getId())).isEmpty();
        }

        @Test
        void 작성자가_아닌_사용자가_삭제하면_예외가_발생해야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(otherUser);

            assertThatThrownBy(() -> marketService.deleteMarketPost(javaMarketItem.getId()))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_게시글을_삭제하면_예외가_발생해야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(sellerUser);

            assertThatThrownBy(() -> marketService.deleteMarketPost(999L))
                    .isInstanceOf(CustomException.class);
        }
    }
}