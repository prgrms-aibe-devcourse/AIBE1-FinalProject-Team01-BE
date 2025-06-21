package kr.co.amateurs.server.service.together;

import jakarta.persistence.EntityManager;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@email.com")
                .password("password")
                .nickname("testUser")
                .name("이름")
                .role(Role.STUDENT)
                .build();
    }


    @Test
    void 검색어없이_전체목록조회하면_모든게시글페이지반환() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(
                createGatheringPost(1L, "첫 번째 모집"),
                createGatheringPost(2L, "두 번째 모집")
        );
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(gatheringRepository.findAllByKeyword(null, expectedPageable)).willReturn(page);

        // when
        Page<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(null, 0, 10, SortType.LATEST);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).title()).isEqualTo("첫 번째 모집");
        assertThat(result.getContent().get(1).title()).isEqualTo("두 번째 모집");
        verify(gatheringRepository).findAllByKeyword(null, expectedPageable);
    }

    @Test
    void 빈검색어로_전체목록조회하면_모든게시글페이지반환() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(
                createGatheringPost(1L, "테스트 모집")
        );
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(gatheringRepository.findAllByKeyword("", expectedPageable)).willReturn(page);


        // when
        Page<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList("", 0, 10, SortType.LATEST);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(gatheringRepository).findAllByKeyword("", expectedPageable);
    }

    @Test
    void 검색어입력하면_해당키워드포함게시글페이지반환() {
        // given
        String keyword = "스터디";
        List<GatheringPost> searchResults = Arrays.asList(
                createGatheringPost(1L, "Java 스터디")
        );
        Page<GatheringPost> page = new PageImpl<>(searchResults);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(gatheringRepository.findAllByKeyword(keyword, expectedPageable)).willReturn(page);

        // when
        Page<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(keyword, 0, 10, SortType.LATEST);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Java 스터디");
        verify(gatheringRepository).findAllByKeyword(keyword, expectedPageable);
    }

    @Test
    void 인기순정렬로_목록조회하면_좋아요수기준내림차순정렬() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(createGatheringPost(1L, "인기 모집"));
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(gatheringRepository.findAllByKeywordOrderByLikeCountDesc(null, expectedPageable)).willReturn(page);

        // when
        Page<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(null, 0, 10, SortType.POPULAR);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(gatheringRepository).findAllByKeywordOrderByLikeCountDesc(null, expectedPageable);
    }

    @Test
    void 조회수순정렬로_목록조회하면_조회수기준내림차순정렬() {
        // given
        List<GatheringPost> gatheringPosts = Arrays.asList(createGatheringPost(1L, "조회 많은 모집"));
        Page<GatheringPost> page = new PageImpl<>(gatheringPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(gatheringRepository.findAllByKeywordOrderByViewCountDesc(null, expectedPageable)).willReturn(page);

        // when
        Page<GatheringPostResponseDTO> result = gatheringService.getGatheringPostList(null, 0, 10, SortType.VIEW_COUNT);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(gatheringRepository).findAllByKeywordOrderByViewCountDesc(null, expectedPageable);
    }

    @Test
    void 존재하는게시글ID로_단건조회하면_해당게시글정보반환() {
        // given

        Post post = createPost(user, "테스트 모집");
        GatheringPost gatheringPost = createGatheringPost(post.getId(), "테스트 모집");
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
        GatheringPost savedGatheringPost = createGatheringPost(1L, "테스트 모집");

        given(userRepository.findById(requestDTO.userId())).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willReturn(savedPost);
        given(gatheringRepository.save(any(GatheringPost.class))).willReturn(savedGatheringPost);

        // when
        GatheringPostResponseDTO result = gatheringService.createGatheringPost(requestDTO);

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
    void 존재하지않는유저로_게시글생성하면_예외발생() {
        // given
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();
        given(userRepository.findById(requestDTO.userId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringService.createGatheringPost(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + requestDTO.userId());
    }

    @Test
    void 존재하는게시글_수정하면_게시글과모집정보모두업데이트() {
        // given
        Long gatheringId = 1L;
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();

        Post post = createPost(user, "기존 제목");
        GatheringPost gatheringPost = createGatheringPostWithPost(gatheringId, post);

        given(gatheringRepository.findById(gatheringId)).willReturn(Optional.of(gatheringPost));

        // when
        gatheringService.updateGatheringPost(gatheringId, requestDTO);

        // then
        verify(gatheringRepository).findById(gatheringId);
    }

    @Test
    void 존재하지않는게시글_수정하면_예외발생() {
        // given
        Long nonExistentId = 999L;
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();
        given(gatheringRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringService.updateGatheringPost(nonExistentId, requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gathering Post not found: " + nonExistentId);
    }

    @Test
    void 존재하는게시글_삭제하면_repository에서삭제호출() {
        // given
        Long gatheringId = 1L;

        // when
        gatheringService.deleteGatheringPost(gatheringId);

        // then
        verify(gatheringRepository).deleteById(gatheringId);
    }

    @Test
    void DTO변환이_올바르게_수행됨() {
        // given

        Post post = createPost(user, "테스트 제목");
        GatheringPost gatheringPost = createGatheringPostWithPost(1L, post);

        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gatheringPost));

        // when
        GatheringPostResponseDTO result = gatheringService.getGatheringPost(1L);

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

    private GatheringPost createGatheringPost(Long id, String title) {

        Post post = createPost(user, title);
        return createGatheringPostWithPost(id, post);
    }

    private GatheringPost createGatheringPostWithPost(Long id, Post post) {
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
                1L, "테스트 모집", "테스트 내용", "태그1,태그2",
                GatheringType.STUDY, GatheringStatus.RECRUITING,
                5, "서울", "1개월", "주 2회"
        );
    }
}