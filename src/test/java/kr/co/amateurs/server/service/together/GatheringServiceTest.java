package kr.co.amateurs.server.service.together;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.like.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.createAdmin;
import static kr.co.amateurs.server.fixture.together.CommonTogetherFixture.createStudent;
import static kr.co.amateurs.server.fixture.together.GatheringTestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GatheringServiceTest {

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private LikeService likeService;

    private User studyUser;
    private User projectUser;
    private User adminUser;
    private Post studyPost;
    private Post projectPost;
    private GatheringPost studyGatheringPost;
    private GatheringPost projectGatheringPost;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        gatheringRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        studyUser = createStudent();
        projectUser = createStudent();
        adminUser = createAdmin();
        studyUser = userRepository.save(studyUser);
        projectUser = userRepository.save(projectUser);
        adminUser = userRepository.save(adminUser);

        studyPost = createStudyPost(studyUser);
        projectPost = createProjectPost(projectUser);

        studyPost = postRepository.save(studyPost);
        projectPost = postRepository.save(projectPost);

        studyGatheringPost = createStudyGatheringPost(studyPost);
        projectGatheringPost = createProjectGatheringPost(projectPost);

        studyGatheringPost = gatheringRepository.save(studyGatheringPost);
        projectGatheringPost = gatheringRepository.save(projectGatheringPost);

        given(likeService.checkHasLiked(studyPost.getId(), studyUser.getId())).willReturn(false);
    }

    @Nested
    class 모임_게시글_목록_조회를 {
        @Test
        void 키워드없이_조회하면_모든_모임_게시글이_반환되어야_한다() {
            // given
            PostPaginationParam paginationParam = PostPaginationParam.builder()
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            // when
            PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(paginationParam);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.pageInfo().getTotalElements()).isEqualTo(2);
            assertThat(result.pageInfo().getPageNumber()).isEqualTo(0);
        }

        @Test
        void 키워드로_조회하면_키워드와_일치하는_모임_게시글이_반환되어야_한다() {
            // given
            PostPaginationParam paginationParam = PostPaginationParam.builder()
                    .keyword("알고리즘")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            // when
            PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(paginationParam);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).title()).contains("알고리즘");
            assertThat(result.content().get(0).gatheringType()).isEqualTo(GatheringType.STUDY);
        }

        @Test
        void 내용으로_검색하면_내용과_일치하는_모임_게시글이_반환되어야_한다() {
            // given
            PostPaginationParam paginationParam = PostPaginationParam.builder()
                    .keyword("React")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            // when
            PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(paginationParam);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).content()).contains("React");
            assertThat(result.content().get(0).gatheringType()).isEqualTo(GatheringType.SIDE_PROJECT);
        }

        @Test
        void 빈_키워드로_조회하면_모든_모임_게시글이_반환되어야_한다() {
            // given
            PostPaginationParam paginationParam = PostPaginationParam.builder()
                    .keyword("")
                    .page(0)
                    .size(10)
                    .sortDirection(Sort.Direction.DESC)
                    .build();

            // when
            PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(paginationParam);

            // then
            assertThat(result.content()).hasSize(2);
        }
    }

    @Nested
    class 모임_게시글_상세_조회를 {
        @Test
        void 존재하는_ID로_조회하면_해당_모임_게시글이_반환되어야_한다() {
            // given
            Long gatheringPostId = studyGatheringPost.getId();
            given(userService.getCurrentLoginUser()).willReturn(studyUser);

            // when
            GatheringPostResponseDTO result = gatheringService.getGatheringPost(gatheringPostId);

            // then
            assertThat(result.postId()).isEqualTo(studyPost.getId());
            assertThat(result.title()).isEqualTo("알고리즘 스터디 모집");
            assertThat(result.gatheringType()).isEqualTo(GatheringType.STUDY);
            assertThat(result.status()).isEqualTo(GatheringStatus.RECRUITING);
            assertThat(result.headCount()).isEqualTo(5);
            assertThat(result.place()).isEqualTo("강남역 스터디카페");
            assertThat(result.nickname()).isEqualTo("student_nickname1");
        }

        @Test
        void 존재하지_않는_ID로_조회하면_예외가_발생해야_한다() {
            // given
            Long nonExistentId = 999L;

            // when & then
            assertThatThrownBy(() -> gatheringService.getGatheringPost(nonExistentId))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 모임_게시글_생성을 {
        @Test
        void 유효한_데이터로_생성하면_새로운_모임_게시글이_생성되어야_한다() {
            // given
            GatheringPostRequestDTO requestDTO = new GatheringPostRequestDTO(
                    "새로운 스터디 모집",
                    "새로운 스터디를 시작합니다.",
                    "Java,Spring",
                    GatheringType.STUDY,
                    GatheringStatus.RECRUITING,
                    6,
                    "홍대입구역",
                    "2025-08-01 ~ 2025-11-30",
                    "매주 수요일 20:00-22:00"
            );
            given(userService.getCurrentLoginUser()).willReturn(studyUser);

            // when
            GatheringPostResponseDTO result = gatheringService.createGatheringPost(requestDTO);

            // then
            assertThat(result.title()).isEqualTo("새로운 스터디 모집");
            assertThat(result.content()).isEqualTo("새로운 스터디를 시작합니다.");
            assertThat(result.gatheringType()).isEqualTo(GatheringType.STUDY);
            assertThat(result.status()).isEqualTo(GatheringStatus.RECRUITING);
            assertThat(result.headCount()).isEqualTo(6);
            assertThat(result.place()).isEqualTo("홍대입구역");

            // DB에 저장되었는지 확인
            assertThat(gatheringRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    class 모임_게시글_수정을 {
        @Test
        void 작성자가_수정하면_게시글이_수정되어야_한다() {
            // given
            Long gatheringPostId = studyGatheringPost.getId();
            GatheringPostRequestDTO updateDTO = new GatheringPostRequestDTO(
                    "수정된 스터디 모집",
                    "수정된 내용입니다.",
                    "Java,Spring,Algorithm",
                    GatheringType.STUDY,
                    GatheringStatus.COMPLETED,
                    3,
                    "서울역",
                    "2025-08-01 ~ 2025-10-31",
                    "매주 목요일 19:00-21:00"
            );
            given(userService.getCurrentLoginUser()).willReturn(studyUser);

            // when
            gatheringService.updateGatheringPost(gatheringPostId, updateDTO);

            // then
            GatheringPost updatedPost = gatheringRepository.findById(gatheringPostId).orElseThrow();
            assertThat(updatedPost.getPost().getTitle()).isEqualTo("수정된 스터디 모집");
            assertThat(updatedPost.getPost().getContent()).isEqualTo("수정된 내용입니다.");
            assertThat(updatedPost.getStatus()).isEqualTo(GatheringStatus.COMPLETED);
            assertThat(updatedPost.getHeadCount()).isEqualTo(3);
            assertThat(updatedPost.getPlace()).isEqualTo("서울역");
        }

        @Test
        void 작성자가_아닌_사용자가_수정하면_예외가_발생해야_한다() {
            // given
            Long gatheringPostId = studyGatheringPost.getId();
            GatheringPostRequestDTO updateDTO = new GatheringPostRequestDTO(
                    "수정된 제목",
                    "수정된 내용",
                    "태그",
                    GatheringType.STUDY,
                    GatheringStatus.RECRUITING,
                    5,
                    "장소",
                    "기간",
                    "일정"
            );
            given(userService.getCurrentLoginUser()).willReturn(projectUser);

            // when & then
            assertThatThrownBy(() -> gatheringService.updateGatheringPost(gatheringPostId, updateDTO))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_게시글을_수정하면_예외가_발생해야_한다() {
            // given
            Long nonExistentId = 999L;
            GatheringPostRequestDTO updateDTO = new GatheringPostRequestDTO(
                    "수정된 제목",
                    "수정된 내용",
                    "태그",
                    GatheringType.STUDY,
                    GatheringStatus.RECRUITING,
                    5,
                    "장소",
                    "기간",
                    "일정"
            );

            // when & then
            assertThatThrownBy(() -> gatheringService.updateGatheringPost(nonExistentId, updateDTO))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 모임_게시글_삭제를 {
        @Test
        void 작성자가_삭제하면_게시글이_삭제되어야_한다() {
            // given
            Long gatheringPostId = studyGatheringPost.getId();
            Long postId = studyPost.getId();
            given(userService.getCurrentLoginUser()).willReturn(studyUser);

            // when
            gatheringService.deleteGatheringPost(gatheringPostId);

            // then
            assertThat(postRepository.findById(postId)).isEmpty();
        }

        @Test
        void 작성자가_아닌_사용자가_삭제하면_예외가_발생해야_한다() {
            // given
            Long gatheringPostId = studyGatheringPost.getId();
            given(userService.getCurrentLoginUser()).willReturn(projectUser);

            // when & then
            assertThatThrownBy(() -> gatheringService.deleteGatheringPost(gatheringPostId))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_게시글을_삭제하면_예외가_발생해야_한다() {
            // given
            Long nonExistentId = 999L;

            // when & then
            assertThatThrownBy(() -> gatheringService.deleteGatheringPost(nonExistentId))
                    .isInstanceOf(CustomException.class);
        }
    }
}