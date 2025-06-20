package kr.co.amateurs.server.service.together;

import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MarketServiceTest {

    @Mock
    private MarketRepository marketRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MarketService marketService;

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
        List<MarketItem> marketPosts = Arrays.asList(
                createMarketPost(1L, "첫 번째 물건"),
                createMarketPost(2L, "두 번째 물건")
        );
        Page<MarketItem> page = new PageImpl<>(marketPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(marketRepository.findAllByKeyword(null, expectedPageable)).willReturn(page);

        // when
        Page<MarketPostResponseDTO> result = marketService.getMarketPostList(null, 0, 10, SortType.LATEST);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).title()).isEqualTo("첫 번째 물건");
        assertThat(result.getContent().get(1).title()).isEqualTo("두 번째 물건");
        verify(marketRepository).findAllByKeyword(null, expectedPageable);
    }

    @Test
    void 빈검색어로_전체목록조회하면_모든게시글페이지반환() {
        // given
        List<MarketItem> marketPosts = Arrays.asList(
                createMarketPost(1L, "테스트 물건")
        );
        Page<MarketItem> page = new PageImpl<>(marketPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(marketRepository.findAllByKeyword("", expectedPageable)).willReturn(page);

        // when
        Page<MarketPostResponseDTO> result = marketService.getMarketPostList("", 0, 10, SortType.LATEST);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(marketRepository).findAllByKeyword("", expectedPageable);
    }

    @Test
    void 검색어입력하면_해당키워드포함게시글페이지반환() {
        // given
        String keyword = "책";
        List<MarketItem> searchResults = Arrays.asList(
                createMarketPost(1L, "Java 책")
        );
        Page<MarketItem> page = new PageImpl<>(searchResults);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(marketRepository.findAllByKeyword(keyword, expectedPageable)).willReturn(page);

        // when
        Page<MarketPostResponseDTO> result = marketService.getMarketPostList(keyword, 0, 10, SortType.LATEST);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Java 책");
        verify(marketRepository).findAllByKeyword(keyword, expectedPageable);
    }

    @Test
    void 인기순정렬로_목록조회하면_좋아요수기준내림차순정렬() {
        // given
        List<MarketItem> marketPosts = Arrays.asList(createMarketPost(1L, "인기 물건"));
        Page<MarketItem> page = new PageImpl<>(marketPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(marketRepository.findAllByKeywordOrderByLikeCountDesc(null, expectedPageable)).willReturn(page);

        // when
        Page<MarketPostResponseDTO> result = marketService.getMarketPostList(null, 0, 10, SortType.POPULAR);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(marketRepository).findAllByKeywordOrderByLikeCountDesc(null, expectedPageable);
    }

    @Test
    void 조회수순정렬로_목록조회하면_조회수기준내림차순정렬() {
        // given
        List<MarketItem> marketPosts = Arrays.asList(createMarketPost(1L, "조회 많은 물건"));
        Page<MarketItem> page = new PageImpl<>(marketPosts);
        Pageable expectedPageable = PageRequest.of(0, 10);

        given(marketRepository.findAllByKeywordOrderByViewCountDesc(null, expectedPageable)).willReturn(page);

        // when
        Page<MarketPostResponseDTO> result = marketService.getMarketPostList(null, 0, 10, SortType.VIEW_COUNT);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(marketRepository).findAllByKeywordOrderByViewCountDesc(null, expectedPageable);
    }

    @Test
    void 존재하는게시글ID로_단건조회하면_해당게시글정보반환() {
        // given
        
        Post post = createPost(user, "테스트 물건");
        MarketItem marketPost = createMarketPost(post.getId(), "테스트 물건");
        Long marketId = marketPost.getId();

        given(marketRepository.findById(marketId)).willReturn(Optional.of(marketPost));

        // when
        MarketPostResponseDTO result = marketService.getMarketPost(marketId);

        // then
        assertThat(result.postId()).isEqualTo(marketId);
        assertThat(result.title()).isEqualTo("테스트 물건");
        verify(marketRepository).findById(marketId);
    }

    @Test
    void 존재하지않는게시글ID로_단건조회하면_예외발생() {
        // given
        Long nonExistentId = 999L;
        given(marketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> marketService.getMarketPost(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Post not found: " + nonExistentId);
    }

    @Test
    void 유효한데이터로_게시글생성하면_게시글과물건정보모두저장() {
        // given
        MarketPostRequestDTO requestDTO = createMarketPostRequestDTO();
        
        Post savedPost = createPost(user, "테스트 물건");
        MarketItem savedMarketItem = createMarketPost(1L, "테스트 물건");

        given(userRepository.findById(requestDTO.userId())).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willReturn(savedPost);
        given(marketRepository.save(any(MarketItem.class))).willReturn(savedMarketItem);

        // when
        MarketPostResponseDTO result = marketService.createMarketPost(requestDTO);

        // then
        assertThat(result.title()).isEqualTo(requestDTO.title());
        assertThat(result.content()).isEqualTo(requestDTO.content());
        assertThat(result.status()).isEqualTo(MarketStatus.SELLING);

        // Post 저장 검증
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();
        assertThat(capturedPost.getTitle()).isEqualTo(requestDTO.title());
        assertThat(capturedPost.getBoardType()).isEqualTo(BoardType.MARKET);

        // MarketItem 저장 검증
        ArgumentCaptor<MarketItem> marketCaptor = ArgumentCaptor.forClass(MarketItem.class);
        verify(marketRepository).save(marketCaptor.capture());
        MarketItem capturedMarket = marketCaptor.getValue();
        assertThat(capturedMarket.getStatus()).isEqualTo(MarketStatus.SELLING);
    }

    @Test
    void 존재하지않는유저로_게시글생성하면_예외발생() {
        // given
        MarketPostRequestDTO requestDTO = createMarketPostRequestDTO();
        given(userRepository.findById(requestDTO.userId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> marketService.createMarketPost(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + requestDTO.userId());
    }

    @Test
    void 존재하는게시글_수정하면_게시글과물건정보모두업데이트() {
        // given
        Long marketId = 1L;
        MarketPostRequestDTO requestDTO = createMarketPostRequestDTO();
        
        Post post = createPost(user, "기존 제목");
        MarketItem marketPost = createMarketPostWithPost(marketId, post);

        given(marketRepository.findById(marketId)).willReturn(Optional.of(marketPost));

        // when
        marketService.updateMarketPost(marketId, requestDTO);

        // then
        verify(marketRepository).findById(marketId);
    }

    @Test
    void 존재하지않는게시글_수정하면_예외발생() {
        // given
        Long nonExistentId = 999L;
        MarketPostRequestDTO requestDTO = createMarketPostRequestDTO();
        given(marketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> marketService.updateMarketPost(nonExistentId, requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Market Post not found: " + nonExistentId);
    }

    @Test
    void 존재하는게시글_삭제하면_repository에서삭제호출() {
        // given
        Long marketId = 1L;

        // when
        marketService.deleteMarketPost(marketId);

        // then
        verify(marketRepository).deleteById(marketId);
    }

    @Test
    void DTO변환이_올바르게_수행됨() {
        // given
        
        Post post = createPost(user, "테스트 제목");
        MarketItem marketPost = createMarketPostWithPost(1L, post);

        given(marketRepository.findById(1L)).willReturn(Optional.of(marketPost));

        // when
        MarketPostResponseDTO result = marketService.getMarketPost(1L);

        // then
        assertThat(result.postId()).isEqualTo(post.getId());
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.title()).isEqualTo(post.getTitle());
        assertThat(result.content()).isEqualTo(post.getContent());
        assertThat(result.tags()).isEqualTo(post.getTags());
        assertThat(result.viewCount()).isEqualTo(post.getViewCount());
        assertThat(result.likeCount()).isEqualTo(post.getLikeCount());
        assertThat(result.status()).isEqualTo(marketPost.getStatus());
        assertThat(result.price()).isEqualTo(marketPost.getPrice());
        assertThat(result.place()).isEqualTo(marketPost.getPlace());
        assertThat(result.createdAt()).isEqualTo(post.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(post.getUpdatedAt());
    }


    private Post createPost(User user, String title) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.MARKET)
                .title(title)
                .content("테스트 내용")
                .tags("태그1,태그2")
                .viewCount(0)
                .likeCount(0)
                .isDeleted(false)
                .build();

    }

    private MarketItem createMarketPost(Long id, String title) {
        
        Post post = createPost(user, title);
        return createMarketPostWithPost(id, post);
    }

    private MarketItem createMarketPostWithPost(Long id, Post post) {
        return MarketItem.builder()
                .post(post)
                .status(MarketStatus.SELLING)
                .price(1000)
                .place("서울")
                .build();
    }

    private MarketPostRequestDTO createMarketPostRequestDTO() {
        return new MarketPostRequestDTO(
                1L, "테스트 물건", "테스트 내용", "태그1,태그2",
                MarketStatus.SELLING,
                1000, "서울"
        );
    }
}
