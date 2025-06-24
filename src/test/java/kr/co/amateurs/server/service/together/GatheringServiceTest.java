package kr.co.amateurs.server.service.together;

import jakarta.persistence.EntityManager;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GatheringServiceTest {

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GatheringService gatheringService;

    private User user;
    private CustomUserDetails currentUser;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@email.com")
                .password("password")
                .nickname("testUser")
                .name("이름")
                .role(Role.STUDENT)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        currentUser = new CustomUserDetails(user);
    }


    @Test
    void 검색어없이_전체목록조회하면_모든게시글페이지반환() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(
                createGatheringPost("첫 번째 모집"),
                createGatheringPost("두 번째 모집")
        );
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);
        Pageable expectedPageable = paginationParam.toPageable();

        given(gatheringRepository.findAllByKeyword(isNull(), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(null, paginationParam);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).title()).isEqualTo("첫 번째 모집");
        assertThat(result.content().get(1).title()).isEqualTo("두 번째 모집");
        verify(gatheringRepository).findAllByKeyword(null, expectedPageable);
    }

    @Test
    void 빈검색어로_전체목록조회하면_모든게시글페이지반환() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(
                createGatheringPost("테스트 모집")
        );
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);
        Pageable expectedPageable = paginationParam.toPageable();

        given(gatheringRepository.findAllByKeyword(eq(""), any(Pageable.class))).willReturn(page);


        // when
        PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList("", paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        verify(gatheringRepository).findAllByKeyword("", expectedPageable);
    }

    @Test
    void 검색어입력하면_해당키워드포함게시글페이지반환() {
        // given
        String keyword = "스터디";
        List<GatheringPost> searchResults = Arrays.asList(
                createGatheringPost("Java 스터디")
        );
        Page<GatheringPost> page = new PageImpl<>(searchResults);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);
        Pageable expectedPageable = paginationParam.toPageable();


        given(gatheringRepository.findAllByKeyword(eq(keyword), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(keyword, paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("Java 스터디");
        verify(gatheringRepository).findAllByKeyword(keyword, expectedPageable);
    }

    @Test
    void 인기순정렬로_목록조회하면_좋아요수기준내림차순정렬() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(createGatheringPost( "인기 모집"));
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.POPULAR);
        Pageable expectedPageable = paginationParam.toPageable();


        given(gatheringRepository.findAllByKeywordOrderByLikeCountDesc(isNull(), any(Pageable.class))).willReturn(page);


        // when
        PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(null, paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        verify(gatheringRepository).findAllByKeywordOrderByLikeCountDesc(null, expectedPageable);
    }

    @Test
    void 조회수순정렬로_목록조회하면_조회수기준내림차순정렬() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(createGatheringPost("조회 많은 모집"));
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.MOST_VIEW);
        Pageable expectedPageable = paginationParam.toPageable();


        given(gatheringRepository.findAllByKeywordOrderByViewCountDesc(isNull(), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(null, paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        verify(gatheringRepository).findAllByKeywordOrderByViewCountDesc(null, expectedPageable);
    }

    @Test
    void 존재하는게시글ID로_단건조회하면_해당게시글정보반환() {
        // given

        Post post = createPost(user, "테스트 모집");
        GatheringPost gatheringPost = createGatheringPost("테스트 모집");
        Long gatheringId = gatheringPost.getId();

        given(gatheringRepository.findById(gatheringId)).willReturn(Optional.of(gatheringPost));

        // when
        GatheringPostResponseDTO result = gatheringService.getGatheringPost(gatheringId);

        // then
        assertThat(result.postId()).isEqualTo(gatheringId);
        assertThat(result.title()).isEqualTo("테스트 모집");
        verify(gatheringRepository).findById(gatheringId);
    }

    @Test
    void 존재하지않는게시글ID로_단건조회하면_예외발생() {
        // given
        Long nonExistentId = 999L;


        given(gatheringRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringService.getGatheringPost(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Post not found: " + nonExistentId);
    }

    @Test
    void 유효한데이터로_게시글생성하면_게시글과모집정보모두저장() {
        // given
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();

        Post savedPost = createPost(user, "테스트 모집");
        GatheringPost savedGatheringPost = createGatheringPost("테스트 모집");

        given(postRepository.save(any(Post.class))).willReturn(savedPost);
        given(gatheringRepository.save(any(GatheringPost.class))).willReturn(savedGatheringPost);

        // when
        GatheringPostResponseDTO result = gatheringService.createGatheringPost(currentUser, requestDTO);

        // then
        assertThat(result.title()).isEqualTo(requestDTO.title());
        assertThat(result.content()).isEqualTo(requestDTO.content());
        assertThat(result.gatheringType()).isEqualTo(requestDTO.gatheringType());
        assertThat(result.status()).isEqualTo(GatheringStatus.RECRUITING);

        // Post 저장 검증
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();
        assertThat(capturedPost.getTitle()).isEqualTo(requestDTO.title());
        assertThat(capturedPost.getBoardType()).isEqualTo(BoardType.GATHER);

        // GatheringPost 저장 검증
        ArgumentCaptor<GatheringPost> gatheringCaptor = ArgumentCaptor.forClass(GatheringPost.class);
        verify(gatheringRepository).save(gatheringCaptor.capture());
        GatheringPost capturedGathering = gatheringCaptor.getValue();
        assertThat(capturedGathering.getGatheringType()).isEqualTo(requestDTO.gatheringType());
        assertThat(capturedGathering.getStatus()).isEqualTo(GatheringStatus.RECRUITING);
    }


    @Test
    void 존재하는게시글_수정하면_게시글과모집정보모두업데이트() {
        // given
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();

        Post post = createPost(user, "기존 제목");
        GatheringPost gatheringPost = createGatheringPostWithPost(post);

        given(gatheringRepository.findByPostId(post.getId())).willReturn(gatheringPost);

        // when
        gatheringService.updateGatheringPost(currentUser, post.getId(), requestDTO);

        // then
        verify(gatheringRepository).findByPostId(post.getId());
    }

    @Test
    void 존재하지않는게시글_수정하면_예외발생() {
        // given
        Long nonExistentId = 999L;
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();
        given(gatheringRepository.findByPostId(nonExistentId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> gatheringService.updateGatheringPost(currentUser, nonExistentId, requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gathering Post not found: " + nonExistentId);
    }

    @Test
    void 존재하는게시글_삭제하면_repository에서삭제호출() {
        // given
        Long postId = 1L;
        Long gatheringId = 1L;
        Post post = createPost(user, "기존 제목");
        ReflectionTestUtils.setField(post, "id", postId);
        GatheringPost gatheringPost = createGatheringPostWithPost(post);
        ReflectionTestUtils.setField(gatheringPost, "id", gatheringId);
        given(gatheringRepository.findByPostId(postId)).willReturn(gatheringPost);

        // when
        gatheringService.deleteGatheringPost(currentUser, postId);

        // then
        verify(gatheringRepository).deleteById(gatheringId);
    }

    @Test
    void DTO변환이_올바르게_수행됨() {
        // given

        Post post = createPost(user, "테스트 제목");
        GatheringPost gatheringPost = createGatheringPostWithPost(post);

        given(gatheringRepository.findById(gatheringPost.getId())).willReturn(Optional.of(gatheringPost));

        // when
        GatheringPostResponseDTO result = gatheringService.getGatheringPost(gatheringPost.getId());

        // then
        assertThat(result.postId()).isEqualTo(post.getId());
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.title()).isEqualTo(post.getTitle());
        assertThat(result.content()).isEqualTo(post.getContent());
        assertThat(result.tags()).isEqualTo(post.getTags());
        assertThat(result.viewCount()).isEqualTo(post.getViewCount());
        assertThat(result.likeCount()).isEqualTo(post.getLikeCount());
        assertThat(result.gatheringType()).isEqualTo(gatheringPost.getGatheringType());
        assertThat(result.status()).isEqualTo(gatheringPost.getStatus());
        assertThat(result.headCount()).isEqualTo(gatheringPost.getHeadCount());
        assertThat(result.place()).isEqualTo(gatheringPost.getPlace());
        assertThat(result.period()).isEqualTo(gatheringPost.getPeriod());
        assertThat(result.schedule()).isEqualTo(gatheringPost.getSchedule());
        assertThat(result.createdAt()).isEqualTo(post.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(post.getUpdatedAt());
    }


    private Post createPost(User user, String title) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.GATHER)
                .title(title)
                .content("테스트 내용")
                .tags("태그1,태그2")
                .viewCount(0)
                .likeCount(0)
                .isDeleted(false)
                .build();
    }

    private GatheringPost createGatheringPost(String title) {

        Post post = createPost(user, title);
        return createGatheringPostWithPost(post);
    }

    private GatheringPost createGatheringPostWithPost(Post post) {
        return GatheringPost.builder()
                .post(post)
                .gatheringType(GatheringType.STUDY)
                .status(GatheringStatus.RECRUITING)
                .headCount(5)
                .place("서울")
                .period("1개월")
                .schedule("주 2회")
                .build();
    }

    private GatheringPostRequestDTO createGatheringPostRequestDTO() {
        return new GatheringPostRequestDTO(
                "테스트 모집", "테스트 내용", "태그1,태그2",
                GatheringType.STUDY, GatheringStatus.RECRUITING,
                5, "서울", "1개월", "주 2회"
        );
    }
}