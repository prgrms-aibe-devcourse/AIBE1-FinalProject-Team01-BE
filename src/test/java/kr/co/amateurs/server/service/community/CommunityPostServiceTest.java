package kr.co.amateurs.server.service.community;

import kr.co.amateurs.server.domain.dto.community.CommunityPageDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostImage;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommunityPostService communityPostService;

    private User testUser;
    private Post testPost;
    private CommunityRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .nickname("testUser")
                .email("test@test.com")
                .build();

        testPost = Post.builder()
                .user(testUser)
                .title("테스트 제목")
                .content("테스트 내용")
                .tags("테스트태그")
                .boardType(BoardType.FREE)
                .viewCount(10)
                .likeCount(5)
                .comments(new ArrayList<>())
                .postImages(new ArrayList<>())
                .build();

        testRequestDTO = new CommunityRequestDTO(
                "새로운 제목",
                "새로운태그",
                "새로운 내용"
        );
    }

    @Test
    void 유저가_키워드없이_검색하면_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        SortType sortType = SortType.LATEST;

        List<Post> postList = List.of(testPost);
        Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(page, pageSize), 1);

        Pageable expectedPageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        given(postRepository.findByBoardType(boardType, expectedPageable))
                .willReturn(postPage);

        // when
        CommunityPageDTO result = communityPostService.searchPosts(null, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.communityList()).hasSize(1);
        assertThat(result.currentPage()).isEqualTo(page);
        assertThat(result.pageSize()).isEqualTo(pageSize);
        assertThat(result.totalPages()).isEqualTo(1);

        verify(postRepository, times(1)).findByBoardType(eq(boardType), any(Pageable.class));
    }

    @Test
    void 유저가_키워드로_검색하면_해당_게시글목록이_반환되어야_한다() {
        // given
        String keyword = "테스트";
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        SortType sortType = SortType.LATEST;

        List<Post> postList = List.of(testPost);
        Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(page, pageSize), 1);

        given(postRepository.findByContentAndBoardType(anyString(), any(BoardType.class), any(Pageable.class)))
                .willReturn(postPage);

        // when
        CommunityPageDTO result = communityPostService.searchPosts(keyword, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.communityList()).hasSize(1);
        assertThat(result.currentPage()).isEqualTo(page);
        assertThat(result.pageSize()).isEqualTo(pageSize);
        assertThat(result.totalPages()).isEqualTo(1);

        verify(postRepository, times(1)).findByContentAndBoardType(eq(keyword), eq(boardType), any(Pageable.class));
    }

    @Test
    void 유저가_유효한_postId로_조회하면_게시글상세가_반환되어야_한다() {
        // given
        Long postId = 1L;
        BoardType boardType = BoardType.FREE;

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

        // when
        CommunityResponseDTO result = communityPostService.getPost(boardType, postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("테스트 제목");
        assertThat(result.content()).isEqualTo("테스트 내용");
        assertThat(result.authorName()).isEqualTo("testUser");
        assertThat(result.boardType()).isEqualTo(BoardType.FREE);
        assertThat(result.hasLiked()).isFalse();

        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    void 유저가_존재하지않는_postId로_조회하면_예외가_발생해야_한다() {
        // given
        Long postId = 999L;
        BoardType boardType = BoardType.FREE;

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> communityPostService.getPost(boardType, postId))
                .isInstanceOf(CustomException.class);

        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    void 유저가_유효한_요청으로_게시글을_생성하면_게시글이_생성되어야_한다() {
        // given
        BoardType boardType = BoardType.FREE;

        given(userRepository.findByNickname("testUser")).willReturn(Optional.of(testUser));
        given(postRepository.save(any(Post.class))).willReturn(testPost);

        // when
        CommunityResponseDTO result = communityPostService.createPost(testRequestDTO, boardType);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("새로운 제목");
        assertThat(result.content()).isEqualTo("새로운 내용");
        assertThat(result.boardType()).isEqualTo(BoardType.FREE);

        verify(userRepository, times(1)).findByNickname("testUser");
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void 유저가_존재하지않는_사용자로_게시글을_생성하면_예외가_발생해야_한다() {
        // given
        BoardType boardType = BoardType.FREE;

        given(userRepository.findByNickname("testUser")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> communityPostService.createPost(testRequestDTO, boardType))
                .isInstanceOf(CustomException.class);

        verify(userRepository, times(1)).findByNickname("testUser");
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void 유저가_게시글을_수정하면_게시글이_수정되어야_한다() {
        // given
        Long postId = 1L;

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> communityPostService.updatePost(testRequestDTO, postId));

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void 유저가_존재하지않는_postId로_수정하면_예외가_발생해야_한다() {
        // given
        Long postId = 999L;

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> communityPostService.updatePost(testRequestDTO, postId))
                .isInstanceOf(CustomException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void 유저가_유효한_postId로_삭제하면_게시글이_삭제되어야_한다() {
        // given
        Long postId = 1L;

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

        // when
        communityPostService.deletePost(postId);

        // then
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).delete(testPost);
    }

    @Test
    void 유저가_존재하지않는_postId로_삭제하면_예외가_발생해야_한다() {
        // given
        Long postId = 999L;

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> communityPostService.deletePost(postId))
                .isInstanceOf(CustomException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void 유저가_이미지있는_게시글을_조회하면_썸네일과_댓글수가_반환되어야_한다() {
        // given
        PostImage postImage = PostImage.builder()
                .imageUrl("https://example.com/image.jpg")
                .post(testPost)
                .build();

        testPost.getPostImages().add(postImage);

        Comment comment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글")
                .build();

        testPost.getComments().add(comment);

        given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

        // when
        CommunityResponseDTO result = communityPostService.getPost(BoardType.FREE, 1L);

        // then
        assertThat(result.hasImages()).isTrue();
        assertThat(result.thumbnailImage()).isEqualTo("https://example.com/image.jpg");
        assertThat(result.commentCount()).isEqualTo(1);
    }

    @Test
    void 유저가_최신순으로_정렬하면_최신순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        SortType sortType = SortType.LATEST;

        List<Post> postList = List.of(testPost);
        Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(page, pageSize), 1);

        given(postRepository.findByBoardType(any(BoardType.class), any(Pageable.class)))
                .willReturn(postPage);

        // when
        CommunityPageDTO result = communityPostService.searchPosts(null, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();

        verify(postRepository, times(1)).findByBoardType(eq(boardType), argThat(pageable -> {
            Sort sort = pageable.getSort();
            return sort.getOrderFor("createdAt") != null &&
                    sort.getOrderFor("createdAt").getDirection() == Sort.Direction.DESC;
        }));
    }

    @Test
    void 유저가_인기순으로_정렬하면_인기순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        SortType sortType = SortType.POPULAR;

        List<Post> postList = List.of(testPost);
        Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(page, pageSize), 1);

        given(postRepository.findByBoardType(any(BoardType.class), any(Pageable.class)))
                .willReturn(postPage);

        // when
        CommunityPageDTO result = communityPostService.searchPosts(null, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();

        verify(postRepository, times(1)).findByBoardType(eq(boardType), argThat(pageable -> {
            Sort sort = pageable.getSort();
            return sort.getOrderFor("likeCount") != null &&
                    sort.getOrderFor("likeCount").getDirection() == Sort.Direction.DESC;
        }));
    }

    @Test
    void 유저가_조회순으로_정렬하면_조회순_게시글목록이_반환되어야_한다() {
        // given
        int page = 0;
        int pageSize = 10;
        BoardType boardType = BoardType.FREE;
        SortType sortType = SortType.VIEW_COUNT;

        List<Post> postList = List.of(testPost);
        Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(page, pageSize), 1);

        given(postRepository.findByBoardType(any(BoardType.class), any(Pageable.class)))
                .willReturn(postPage);

        // when
        CommunityPageDTO result = communityPostService.searchPosts(null, page, boardType, sortType, pageSize);

        // then
        assertThat(result).isNotNull();

        verify(postRepository, times(1)).findByBoardType(eq(boardType), argThat(pageable -> {
            Sort sort = pageable.getSort();
            return sort.getOrderFor("viewCount") != null &&
                    sort.getOrderFor("viewCount").getDirection() == Sort.Direction.DESC;
        }));
    }
}