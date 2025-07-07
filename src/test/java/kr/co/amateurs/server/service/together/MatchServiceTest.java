package kr.co.amateurs.server.service.together;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
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


import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.createAdmin;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.createStudent;
import static kr.co.amateurs.server.fixture.together.MatchTestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchServiceTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

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

    private User coffeeUser;
    private User mentoringUser;
    private User adminUser;
    private Post coffeePost;
    private Post mentoringPost;
    private MatchingPost coffeeMatching;
    private MatchingPost mentoringMatching;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        matchRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        coffeeUser = createStudent();
        mentoringUser = createStudent();
        adminUser = createAdmin();
        coffeeUser = userRepository.save(coffeeUser);
        mentoringUser = userRepository.save(mentoringUser);
        adminUser = userRepository.save(adminUser);

        coffeePost = createCoffeeChatPost(coffeeUser);
        mentoringPost = createMentoringPost(coffeeUser);

        coffeePost = postRepository.save(coffeePost);
        mentoringPost = postRepository.save(mentoringPost);

        coffeeMatching = createCoffeeChatMatchingPost(coffeePost);
        mentoringMatching = createMentoringMatchingPost(mentoringPost);

        coffeeMatching = matchRepository.save(coffeeMatching);
        mentoringMatching = matchRepository.save(mentoringMatching);

        given(likeService.checkHasLiked(coffeePost.getId(), coffeeUser.getId())).willReturn(false);
        given(likeService.checkHasLiked(mentoringPost.getId(), coffeeUser.getId())).willReturn(false);
    }

    @Nested
    class 모임_게시글_목록_조회를 {
        @Test
        void 키워드없이_조회하면_모든_모임_게시글이_반환되어야_한다() {
            PostPaginationParam param = PostPaginationParam.builder()
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(param);

            assertThat(result.content()).hasSize(2);
            assertThat(result.pageInfo().getTotalElements()).isEqualTo(2);
            assertThat(result.pageInfo().getPageNumber()).isEqualTo(0);
        }

        @Test
        void 제목으로_조회하면_키워드와_일치하는_모임_게시글이_반환되어야_한다() {
            PostPaginationParam param = PostPaginationParam.builder()
                    .keyword("커피챗")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(param);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).title()).contains("커피챗");
        }

        @Test
        void 내용으로_검색하면_내용과_일치하는_모임_게시글이_반환되어야_한다() {
            PostPaginationParam param = PostPaginationParam.builder()
                    .keyword("멘토링")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(param);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).content()).contains("멘토링");
        }

        @Test
        void 빈_키워드로_조회하면_모든_모임_게시글이_반환되어야_한다() {
            PostPaginationParam param = PostPaginationParam.builder()
                    .keyword("")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(param);

            assertThat(result.content()).hasSize(2);
        }
    }

    @Nested
    class 모임_게시글_상세_조회를 {
        @Test
        void 존재하는_ID로_조회하면_해당_모임_게시글이_반환되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(coffeeUser);

            MatchPostResponseDTO dto = matchService.getMatchPost(coffeeMatching.getId());

            assertThat(dto.postId()).isEqualTo(coffeePost.getId());
            assertThat(dto.title()).isEqualTo("커피챗 참가자 모집");
            assertThat(dto.matchingType()).isEqualTo(MatchingType.COFFEE_CHAT);
            assertThat(dto.status()).isEqualTo(MatchingStatus.OPEN);
            assertThat(dto.expertiseArea()).isEqualTo("백엔드");
        }

        @Test
        void 존재하지_않는_ID로_조회하면_예외가_발생해야_한다() {
            assertThatThrownBy(() -> matchService.getMatchPost(999L))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 모임_게시글_생성을 {
        @Test
        void 유효한_데이터로_생성하면_새로운_모임_게시글이_생성되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(coffeeUser);
            MatchPostRequestDTO dto = createMatchPostRequestDTO();

            MatchPostResponseDTO result = matchService.createMatchPost(dto);

            assertThat(result.title()).isEqualTo("Spring 멘토링 모집");
            assertThat(result.content()).isEqualTo("Spring 멘토링 해주실 분 구합니다.");
            assertThat(result.matchingType()).isEqualTo(MatchingType.MENTORING);
            assertThat(result.status()).isEqualTo(MatchingStatus.OPEN);
            assertThat(result.expertiseArea()).isEqualTo("백엔드");
            assertThat(matchRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    class 모임_게시글_수정을 {
        @Test
        void 작성자가_수정하면_게시글이_수정되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(coffeeUser);
            MatchPostRequestDTO updateDto = new MatchPostRequestDTO(
                    "커피챗 모집 마감",
                    "더 이상 모집하지 않습니다.",
                    "커피챗",
                    MatchingType.COFFEE_CHAT,
                    MatchingStatus.MATCHED,
                    "백엔드"
            );

            matchService.updateMatchPost(coffeeMatching.getId(), updateDto);

            MatchingPost updated = matchRepository.findById(coffeeMatching.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MatchingStatus.MATCHED);
            assertThat(updated.getExpertiseAreas()).isEqualTo("백엔드");
        }

        @Test
        void 작성자가_아닌_사용자가_수정하면_예외가_발생해야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(mentoringUser);
            MatchPostRequestDTO updateDto = new MatchPostRequestDTO(
                    "커피챗 모집 마감",
                    "더 이상 모집하지 않습니다.",
                    "커피챗",
                    MatchingType.COFFEE_CHAT,
                    MatchingStatus.MATCHED,
                    "백엔드"
            );

            assertThatThrownBy(() -> matchService.updateMatchPost(coffeeMatching.getId(), updateDto))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_게시글을_수정하면_예외가_발생해야_한다() {
            MatchPostRequestDTO updateDto = new MatchPostRequestDTO(
                    "제목",
                    "내용",
                    "태그",
                    MatchingType.COFFEE_CHAT,
                    MatchingStatus.OPEN,
                    "분야"
            );
            given(userService.getCurrentLoginUser()).willReturn(coffeeUser);

            assertThatThrownBy(() -> matchService.updateMatchPost(999L, updateDto))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 모임_게시글_삭제를 {
        @Test
        void 작성자가_삭제하면_게시글이_삭제되어야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(coffeeUser);

            matchService.deleteMatchPost(coffeeMatching.getId());

            assertThat(postRepository.findById(coffeePost.getId())).isEmpty();
        }

        @Test
        void 작성자가_아닌_사용자가_삭제하면_예외가_발생해야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(mentoringUser);

            assertThatThrownBy(() -> matchService.deleteMatchPost(coffeeMatching.getId()))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_게시글을_삭제하면_예외가_발생해야_한다() {
            given(userService.getCurrentLoginUser()).willReturn(coffeeUser);

            assertThatThrownBy(() -> matchService.deleteMatchPost(999L))
                    .isInstanceOf(CustomException.class);
        }
    }
}