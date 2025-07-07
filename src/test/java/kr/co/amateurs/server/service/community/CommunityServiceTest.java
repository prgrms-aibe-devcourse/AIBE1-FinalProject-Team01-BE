package kr.co.amateurs.server.service.community;

import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.community.CommunityTestFixtures;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.community.CommunityRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommunityServiceTest {
    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommentRepository commentRepository;

    @MockitoBean
    private UserService userService;

    private User testStudentUser;
    private User testOtherUser;
    private CommunityPost testFreeCommunityPost;
    private CommunityPost testFreeCommunityPost2;
    private CommunityPost testGatherCommunityPost;

    @BeforeEach
    void setUp() throws InterruptedException {
        // 사용자 생성
        testStudentUser = userRepository.save(CommunityTestFixtures.createStudentUser());
        testOtherUser = userRepository.save(CommunityTestFixtures.createCustomUser("other@other.com", "other", "other", Role.STUDENT));

        // Post 생성
        Post testFreePost = postRepository.save(CommunityTestFixtures.createPostWithCounts(testStudentUser, "자유1", "자유1", BoardType.FREE, 10, 10));
        Thread.sleep(10);
        Post testFreePost2 = postRepository.save(CommunityTestFixtures.createPost(testStudentUser, "자유2", "자유2", BoardType.FREE));
        Post testGatherPost = postRepository.save(CommunityTestFixtures.createPost(testStudentUser, "게더1", "게더2", BoardType.GATHER));

        // CommunityPost 생성
        testFreeCommunityPost = communityRepository.save(CommunityTestFixtures.createCommunityPost(testFreePost));
        testFreeCommunityPost2 = communityRepository.save(CommunityTestFixtures.createCommunityPost(testFreePost2));
        testGatherCommunityPost = communityRepository.save(CommunityTestFixtures.createCommunityPost(testGatherPost));
    }

    @Test
    void 유저가_키워드없이_검색하면_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(null)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_LATEST)
                .build();

        // when
        PageResponseDTO<CommunityResponseDTO> result = communityService.searchPosts(boardType, param);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.pageInfo().getPageNumber()).isEqualTo(page);
        assertThat(result.pageInfo().getPageSize()).isEqualTo(pageSize);
        assertThat(result.pageInfo().getTotalPages()).isEqualTo(1);
        assertThat(result.content().get(0).title()).isEqualTo("자유2");
    }

    @Test
    void 유저가_키워드로_검색하면_해당_게시글목록이_반환되어야_한다() {
        // given
        String keyword = "자유";
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(keyword)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_LATEST)
                .build();

        // when
        PageResponseDTO<CommunityResponseDTO> result = communityService.searchPosts(boardType, param);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).title()).contains("자유2");
    }

    @Test
    void 유저가_존재하지않는_키워드로_검색하면_빈_목록이_반환되어야_한다() {
        // given
        String keyword = "함께해요";
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(keyword)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_LATEST)
                .build();

        // when
        PageResponseDTO<CommunityResponseDTO> result = communityService.searchPosts(boardType, param);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
    }

    @Test
    void 유저가_인기순으로_정렬하면_인기순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(null)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_POPULAR)
                .build();

        // when
        PageResponseDTO<CommunityResponseDTO> result = communityService.searchPosts(boardType, param);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).likeCount()).isGreaterThanOrEqualTo(
                result.content().get(1).likeCount()
        );
        assertThat(result.content().get(0).title()).contains("자유1");
    }

    @Test
    void 유저가_조회순으로_정렬하면_조회순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        PostPaginationParam param = PostPaginationParam.builder()
                .keyword(null)
                .page(page)
                .size(pageSize)
                .sortDirection(Sort.Direction.DESC)
                .field(PaginationSortType.POST_MOST_VIEW)
                .build();

        // when
        PageResponseDTO<CommunityResponseDTO> result = communityService.searchPosts(boardType, param);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).viewCount()).isGreaterThanOrEqualTo(
                result.content().get(1).viewCount()
        );
        assertThat(result.content().get(0).title()).contains("자유1");
    }

    @Test
    void 유저가_유효한_communityId로_조회하면_게시글상세가_반환되어야_한다() {
        // given
        Long communityId = testFreeCommunityPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when
        CommunityResponseDTO result = communityService.getPost(communityId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("자유1");
        assertThat(result.content()).isEqualTo("자유1");
        assertThat(result.nickname()).isEqualTo("student");
        assertThat(result.boardType()).isEqualTo(BoardType.FREE);
        assertThat(result.viewCount()).isEqualTo(10);
        assertThat(result.likeCount()).isEqualTo(10);
        assertThat(result.tags()).isEqualTo("테스트,태그");
        assertThat(result.hasLiked()).isFalse();
        assertThat(result.hasBookmarked()).isFalse();
    }

    @Test
    void 유저가_존재하지_않는_communityId로_조회하면_예외가_발생해야_한다() {
        // given
        Long nonExistentCommunityId = 999L;

        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when & then
        assertThatThrownBy(() -> communityService.getPost(nonExistentCommunityId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_유효한_요청으로_게시글을_생성하면_게시글이_생성되어야_한다() {
        // given
        CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO("새로운 제목","태그", "새로운 내용");
        BoardType boardType = BoardType.FREE;

        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when
        CommunityResponseDTO result = communityService.createPost(requestDTO, boardType);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("새로운 제목");
        assertThat(result.tags()).isEqualTo("태그");
        assertThat(result.content()).isEqualTo("새로운 내용");
        assertThat(result.boardType()).isEqualTo(BoardType.FREE);
        assertThat(result.nickname()).isEqualTo("student");
    }

    @Test
    void 유저가_로그인하지않은_상태로_게시글을_생성하면_예외가_발생해야_한다() {
        // given
        CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO("새로운 제목", "태그","새로운 내용");
        BoardType boardType = BoardType.FREE;

        given(userService.getCurrentLoginUser()).willThrow(CustomException.class);

        // when & then
        assertThatThrownBy(() -> communityService.createPost(requestDTO, boardType))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인_게시글을_수정하면_게시글이_수정되어야_한다() {
        // given
        CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO("수정된 제목","수정 태그", "수정된 내용");
        Long communityId = testFreeCommunityPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when
        communityService.updatePost(requestDTO, communityId);

        // then
        CommunityResponseDTO result = communityService.getPost(communityId);
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.tags()).isEqualTo("수정 태그");
        assertThat(result.content()).isEqualTo("수정된 내용");
    }

    @Test
    void 유저가_존재하지_않는_communityId로_수정하면_예외가_발생해야_한다() {
        // given
        CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO("수정된 제목","수정 태그","수정된 내용");
        Long nonExistentCommunityId = 999L;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> communityService.updatePost(requestDTO, nonExistentCommunityId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_다른_게시글을_수정하면_게시글이_예외가_발생해야_한다() {
        // given
        CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO("수정된 제목","수정 태그", "수정된 내용");
        Long communityId = testFreeCommunityPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testOtherUser);

        // when & then
        assertThatThrownBy(() -> communityService.updatePost(requestDTO, communityId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인_게시글을_삭제하면_게시글이_삭제되어야_한다() {
        // given
        Long communityId = testFreeCommunityPost.getId();
        Long postId = testFreeCommunityPost.getPost().getId();
        given(userService.getCurrentLoginUser()).willReturn(testStudentUser);

        // when
        communityService.deletePost(communityId);

        // then
        assertThat(postRepository.findById(postId)).isEmpty();

    }

    @Test
    void 유저가_존재하지_않는_communityId로_삭제하면_예외가_발생해야_한다() {
        // given
        Long nonExistentCommunityId = 999L;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> communityService.deletePost(nonExistentCommunityId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_다른_게시글을_삭제하면_예외가_발생해야_한다() {
        // given
        Long communityId = testFreeCommunityPost.getId();

        given(userService.getCurrentLoginUser()).willReturn(testOtherUser);

        // when & then
        assertThatThrownBy(() -> communityService.deletePost(communityId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void findById로_CommunityPost를_조회할_수_있어야_한다() {
        // given
        Long communityId = testFreeCommunityPost.getId();

        // when
        CommunityPost result = communityService.findById(communityId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(communityId);
        assertThat(result.getPost().getTitle()).isEqualTo("자유1");
    }

    //현재 미구현
//    @Test
//    void increaseViewCount로_조회수가_증가해야_한다() {
//        // given
//        Long communityId = testFreeCommunityPost.getId();
//        int originalViewCount = testFreeCommunityPost.getPost().getViewCount();
//
//        // when
//        communityService.increaseViewCount(communityId);
//
//        // then
//        CommunityResponseDTO result = communityService.getPost(communityId);
//        assertThat(result.viewCount()).isEqualTo(originalViewCount + 1);
//    }
}