package kr.co.amateurs.server.service.comment;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.dto.comment.ReplyCount;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testRootComment;
    private Comment testReplyComment;
    private CommentRequestDTO testRequestDTO;

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

        testRootComment = Comment.builder()
                .user(testUser)
                .post(testPost)
                .content("루트 댓글")
                .likeCount(0)
                .isDeleted(false)
                .build();

        testReplyComment = Comment.builder()
                .user(testUser)
                .post(testPost)
                .parentComment(testRootComment)
                .content("답글 댓글")
                .likeCount(0)
                .isDeleted(false)
                .build();

        ReflectionTestUtils.setField(testRootComment, "id", 1L);
        ReflectionTestUtils.setField(testReplyComment, "id", 2L);

        testRequestDTO = new CommentRequestDTO(null, "새로운 댓글");
    }

    @Test
    void 게시글의_루트_댓글_목록을_조회할수있다() {
        // given
        Long postId = 1L;
        Long cursor = null;
        int size = 5;

        List<Comment> comments = Arrays.asList(testRootComment);
        List<ReplyCount> replyCountData = List.of(
                new ReplyCount() {
                    @Override
                    public Long getParentCommentId() {
                        return testRootComment.getId();
                    }

                    @Override
                    public Long getCount() {
                        return 3L;
                    }
                }
        );


        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(
                eq(testPost), any(PageRequest.class))).willReturn(comments);
        given(commentRepository.countRepliesByParentIds(anyList())).willReturn(replyCountData);

        // when
        CommentPageDTO result = commentService.getCommentsByPostId(postId, cursor, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.comments()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.comments().get(0).content()).isEqualTo("루트 댓글");
        assertThat(result.comments().get(0).replyCount()).isEqualTo(3);
    }

    @Test
    void 커서가_있을때_다음_페이지_댓글을_조회할수있다() {
        // given
        Long postId = 1L;
        Long cursor = 10L;
        int size = 2;

        List<Comment> comments = Arrays.asList(testRootComment, testRootComment, testRootComment); // size+1 개

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findByPostAndParentCommentIsNullAndIdGreaterThanOrderByCreatedAtAsc(
                eq(testPost), eq(cursor), any(PageRequest.class))).willReturn(comments);
        given(commentRepository.countRepliesByParentIds(anyList())).willReturn(new ArrayList<>());

        // when
        CommentPageDTO result = commentService.getCommentsByPostId(postId, cursor, size);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.comments()).hasSize(2);
        assertThat(result.nextCursor()).isNotNull();
    }

    @Test
    void 존재하지_않는_게시글의_댓글을_조회하면_예외가_발생한다() {
        // given
        Long postId = 999L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByPostId(postId, null, 5))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    void 부모_댓글의_답글목록을_조회할수있다() {
        // given
        Long postId = 1L;
        Long cursor = null;
        int size = 5;

        List<Comment> replies = Arrays.asList(testReplyComment);

        given(commentRepository.findById(testReplyComment.getId())).willReturn(Optional.of(testRootComment));
        given(commentRepository.findByParentCommentOrderByCreatedAtAsc(
                eq(testRootComment), any(PageRequest.class))).willReturn(replies);

        // when
        CommentPageDTO result = commentService.getReplies(postId, testReplyComment.getId(), cursor, size);

        // then
        assertThat(result.comments()).hasSize(1);
        assertThat(result.comments().get(0).content()).isEqualTo("답글 댓글");
        assertThat(result.comments().get(0).parentCommentId()).isEqualTo(testReplyComment.getParentComment().getId());
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 존재하지_않는_부모_댓글의_답글을_조회하면_예외가_발생한다() {
        // given
        Long postId = 1L;
        Long parentCommentId = 999L;
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getReplies(postId, parentCommentId, null, 5))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    void 루트_댓글을_생성할수있다() {
        // given
        Long postId = 1L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(null, "새로운 루트 댓글");

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.save(any(Comment.class))).willReturn(testRootComment);

        // when
        CommentResponseDTO result = commentService.createComment(postId, requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("루트 댓글");
        assertThat(result.parentCommentId()).isNull();
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void 유저가_답글을_생성_할_수_있다() {
        // given
        Long postId = 1L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(testRootComment.getId(), "새로운 답글");

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(testRootComment.getId())).willReturn(Optional.of(testRootComment));
        given(commentRepository.save(any(Comment.class))).willReturn(testReplyComment);

        // when
        CommentResponseDTO result = commentService.createComment(postId, requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("답글 댓글");
        assertThat(result.parentCommentId()).isEqualTo(testReplyComment.getParentComment().getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void 부모_댓글을_가지고_있는_답글의_답글을_생성하려하면_예외가_발생한다() {
        // given
        Long postId = 1L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(2L, "답글의 답글");

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(2L)).willReturn(Optional.of(testReplyComment));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, requestDTO))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    void 존재하지_않는_게시글에_댓글을_생성하면_예외가_발생한다() {
        // given
        Long postId = 999L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(null, "댓글");

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, requestDTO))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    void 유저가_본인의_댓글_내용을_수정할수있다() {
        // given
        Long postId = 1L;
        Long commentId = 1L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(null, "수정된 댓글");

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(testRootComment));

        // when
        commentService.updateComment(postId, commentId, requestDTO);

        // then
        assertThat(testRootComment.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    void 존재하지_않는_댓글을_수정_하려하면_예외가_발생한다() {
        // given
        Long postId = 1L;
        Long commentId = 999L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(null, "수정된 댓글");

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(postId, commentId, requestDTO))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    void 유저가_본인의_댓글을_삭제할수있다() {
        // given
        Long postId = 1L;
        Long commentId = 1L;

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(testRootComment));

        // when
        commentService.deleteComment(postId, commentId);

        // then
        verify(commentRepository).delete(testRootComment);
    }

    @Test
    void 존재하지_않는_댓글을_삭제하려하면_예외가_발생한다() {
        // given
        Long postId = 1L;
        Long commentId = 999L;

        given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }
}
