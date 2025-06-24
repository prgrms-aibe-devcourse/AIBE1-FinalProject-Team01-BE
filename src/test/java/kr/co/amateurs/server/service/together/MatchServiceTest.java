package kr.co.amateurs.server.service.together;

import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchService matchService;

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
        List<MatchingPost> matchPosts = Arrays.asList(
                createMatchPost("첫 번째 모집"),
                createMatchPost("두 번째 모집")
        );
        Page<MatchingPost> page = new PageImpl<>(matchPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);
        Pageable expectedPageable = paginationParam.toPageable();

        given(matchRepository.findAllByKeyword(isNull(), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(null, paginationParam);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).title()).isEqualTo("첫 번째 모집");
        assertThat(result.content().get(1).title()).isEqualTo("두 번째 모집");
        verify(matchRepository).findAllByKeyword(null, expectedPageable);
    }

    @Test
    void 빈검색어로_전체목록조회하면_모든게시글페이지반환() {
        // given
        List<MatchingPost> matchPosts = Arrays.asList(
                createMatchPost("테스트 모집")
        );
        Page<MatchingPost> page = new PageImpl<>(matchPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);
        Pageable expectedPageable = paginationParam.toPageable();

        given(matchRepository.findAllByKeyword(eq(""), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList("", paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        verify(matchRepository).findAllByKeyword("", expectedPageable);
    }

    @Test
    void 검색어입력하면_해당키워드포함게시글페이지반환() {
        // given
        String keyword = "커피챗";
        List<MatchingPost> searchResults = Arrays.asList(
                createMatchPost("Java 커피챗")
        );
        Page<MatchingPost> page = new PageImpl<>(searchResults);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);
        Pageable expectedPageable = paginationParam.toPageable();

        given(matchRepository.findAllByKeyword(eq(keyword), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(keyword, paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("Java 커피챗");
        verify(matchRepository).findAllByKeyword(keyword, expectedPageable);
    }

    @Test
    void 인기순정렬로_목록조회하면_좋아요수기준내림차순정렬() {
        // given
        List<MatchingPost> matchPosts = Arrays.asList(createMatchPost("인기 모집"));
        Page<MatchingPost> page = new PageImpl<>(matchPosts);

        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.POPULAR);
        Pageable expectedPageable = paginationParam.toPageable();

        given(matchRepository.findAllByKeywordOrderByLikeCountDesc(isNull(), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(null, paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        verify(matchRepository).findAllByKeywordOrderByLikeCountDesc(null, expectedPageable);
    }

    @Test
    void 조회수순정렬로_목록조회하면_조회수기준내림차순정렬() {
        // given
        List<MatchingPost> matchPosts = Arrays.asList(createMatchPost("조회 많은 모집"));
        Page<MatchingPost> page = new PageImpl<>(matchPosts);

        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.MOST_VIEW);
        Pageable expectedPageable = paginationParam.toPageable();

        given(matchRepository.findAllByKeywordOrderByViewCountDesc(isNull(), any(Pageable.class))).willReturn(page);

        // when
        PageResponseDTO<MatchPostResponseDTO> result = matchService.getMatchPostList(null, paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        verify(matchRepository).findAllByKeywordOrderByViewCountDesc(null, expectedPageable);
    }

    @Test
    void 존재하는게시글ID로_단건조회하면_해당게시글정보반환() {
        // given
        
        Post post = createPost(user, "테스트 모집");
        MatchingPost matchPost = createMatchPost("테스트 모집");
        Long matchId = matchPost.getId();

        given(matchRepository.findById(matchId)).willReturn(Optional.of(matchPost));

        // when
        MatchPostResponseDTO result = matchService.getMatchPost(matchId);

        // then
        assertThat(result.postId()).isEqualTo(matchId);
        assertThat(result.title()).isEqualTo("테스트 모집");
        verify(matchRepository).findById(matchId);
    }

    @Test
    void 존재하지않는게시글ID로_단건조회하면_예외발생() {
        // given
        Long nonExistentId = 999L;
        given(matchRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchService.getMatchPost(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Post not found: " + nonExistentId);
    }

    @Test
    void 유효한데이터로_게시글생성하면_게시글과모집정보모두저장() {
        // given
        MatchPostRequestDTO requestDTO = createMatchPostRequestDTO();
        
        Post savedPost = createPost(user, "테스트 모집");
        MatchingPost savedMatchingPost = createMatchPost("테스트 모집");

        given(postRepository.save(any(Post.class))).willReturn(savedPost);
        given(matchRepository.save(any(MatchingPost.class))).willReturn(savedMatchingPost);

        // when
        MatchPostResponseDTO result = matchService.createMatchPost(currentUser, requestDTO);

        // then
        assertThat(result.title()).isEqualTo(requestDTO.title());
        assertThat(result.content()).isEqualTo(requestDTO.content());
        assertThat(result.matchingType()).isEqualTo(requestDTO.matchingType());
        assertThat(result.status()).isEqualTo(MatchingStatus.OPEN);

        // Post 저장 검증
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();
        assertThat(capturedPost.getTitle()).isEqualTo(requestDTO.title());
        assertThat(capturedPost.getBoardType()).isEqualTo(BoardType.MATCH);

        // MatchingPost 저장 검증
        ArgumentCaptor<MatchingPost> matchCaptor = ArgumentCaptor.forClass(MatchingPost.class);
        verify(matchRepository).save(matchCaptor.capture());
        MatchingPost capturedMatch = matchCaptor.getValue();
        assertThat(capturedMatch.getMatchingType()).isEqualTo(requestDTO.matchingType());
        assertThat(capturedMatch.getStatus()).isEqualTo(MatchingStatus.OPEN);
    }


    @Test
    void 존재하는게시글_수정하면_게시글과모집정보모두업데이트() {
        // given
        MatchPostRequestDTO requestDTO = createMatchPostRequestDTO();
        
        Post post = createPost(user, "기존 제목");
        MatchingPost matchPost = createMatchPostWithPost(post);

        given(matchRepository.findByPostId(post.getId())).willReturn(matchPost);

        // when
        matchService.updateMatchPost(currentUser, post.getId(), requestDTO);

        // then
        verify(matchRepository).findByPostId(post.getId());
    }

    @Test
    void 존재하지않는게시글_수정하면_예외발생() {
        // given
        Long nonExistentId = 999L;
        MatchPostRequestDTO requestDTO = createMatchPostRequestDTO();
        given(matchRepository.findByPostId(nonExistentId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> matchService.updateMatchPost(currentUser, nonExistentId, requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Match Post not found: " + nonExistentId);
    }

    @Test
    void 존재하는게시글_삭제하면_repository에서삭제호출() {
        // given
        Long postId = 1L;
        Long matchId = 1L;
        Post post = createPost(user, "기존 제목");
        ReflectionTestUtils.setField(post, "id", postId);
        MatchingPost matchPost = createMatchPostWithPost(post);
        ReflectionTestUtils.setField(matchPost, "id", matchId);
        given(matchRepository.findByPostId(postId)).willReturn(matchPost);

        // when
        matchService.deleteMatchPost(currentUser, postId);

        // then
        verify(matchRepository).deleteById(matchId);
    }

    @Test
    void DTO변환이_올바르게_수행됨() {
        // given
        
        Post post = createPost(user, "테스트 제목");
        MatchingPost matchPost = createMatchPostWithPost(post);

        given(matchRepository.findById(1L)).willReturn(Optional.of(matchPost));

        // when
        MatchPostResponseDTO result = matchService.getMatchPost(1L);

        // then
        assertThat(result.postId()).isEqualTo(post.getId());
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.title()).isEqualTo(post.getTitle());
        assertThat(result.content()).isEqualTo(post.getContent());
        assertThat(result.tags()).isEqualTo(post.getTags());
        assertThat(result.viewCount()).isEqualTo(post.getViewCount());
        assertThat(result.likeCount()).isEqualTo(post.getLikeCount());
        assertThat(result.matchingType()).isEqualTo(matchPost.getMatchingType());
        assertThat(result.status()).isEqualTo(matchPost.getStatus());
        assertThat(result.expertiseArea()).isEqualTo(matchPost.getExpertiseAreas());
        assertThat(result.createdAt()).isEqualTo(post.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(post.getUpdatedAt());
    }


    private Post createPost(User user, String title) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.MATCH)
                .title(title)
                .content("테스트 내용")
                .tags("태그1,태그2")
                .viewCount(0)
                .likeCount(0)
                .isDeleted(false)
                .build();
    }

    private MatchingPost createMatchPost(String title) {
        
        Post post = createPost(user, title);
        return createMatchPostWithPost(post);
    }

    private MatchingPost createMatchPostWithPost(Post post) {
        return MatchingPost.builder()
                .post(post)
                .matchingType(MatchingType.COFFEE_CHAT)
                .status(MatchingStatus.OPEN)
                .expertiseAreas("서울")
                .build();
    }

    private MatchPostRequestDTO createMatchPostRequestDTO() {
        return new MatchPostRequestDTO(
                "테스트 모집", "테스트 내용", "태그1,태그2",
                MatchingType.COFFEE_CHAT, MatchingStatus.OPEN, "서울"
        );
    }
}
