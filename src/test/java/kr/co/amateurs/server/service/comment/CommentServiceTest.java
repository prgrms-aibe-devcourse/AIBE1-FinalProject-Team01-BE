package kr.co.amateurs.server.service.comment;

import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.comment.CommentTestFixtures;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @MockitoBean
    private UserService userService;

    private User testStudentUser;
    private User testOtherUser;
    private Post testPost;
    private Comment testRootComment;
    private Comment testReplyComment;

    @BeforeEach
    void setUp() {
        testStudentUser = userRepository.save(CommentTestFixtures.createStudentUser());
        testOtherUser = userRepository.save(CommentTestFixtures.createAnotherUser());
        testPost = postRepository.save(CommentTestFixtures.createTestPost(testStudentUser));
        testRootComment = commentRepository.save(CommentTestFixtures.createRootComment(testPost, testStudentUser, "루트 댓글"));
        testReplyComment = commentRepository.save(CommentTestFixtures.createReplyComment(testPost, testStudentUser, testRootComment, "답글 댓글"));
    }

    @Test
    void 게시글의_루트_댓글_목록을_조회할수있다() {
        // given
        Long postId = testPost.getId();
        Long cursor = null;
        int size = 5;

        // when
        CommentPageDTO result = commentService.getCommentsByPostId(postId, cursor, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.comments()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.comments().get(0).content()).isEqualTo("루트 댓글");
        assertThat(result.comments().get(0).nickname()).isEqualTo("student");
        assertThat(result.comments().get(0).parentCommentId()).isNull();
    }

    @Test
    void 커서가_있을때_다음_페이지_댓글을_조회할수있다() {
        // given
        Long postId = testPost.getId();
        Comment comment1 = commentRepository.save(CommentTestFixtures.createRootComment(testPost, testStudentUser, "댓글1"));
        Comment comment2 = commentRepository.save(CommentTestFixtures.createRootComment(testPost, testStudentUser, "댓글2"));
        Comment comment3 = commentRepository.save(CommentTestFixtures.createRootComment(testPost, testStudentUser, "댓글3"));

        Long cursor = comment1.getId();
        int size = 1;

        // when
        CommentPageDTO result = commentService.getCommentsByPostId(postId, cursor, size);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.comments()).hasSize(1);
        assertThat(result.nextCursor()).isNotNull();
    }

    @Test
    void 존재하지_않는_게시글의_댓글을_조회하면_예외가_발생한다() {
        // given
        Long nonExistentPostId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByPostId(nonExistentPostId, null, 5))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 부모_댓글의_답글목록을_조회할수있다() {
        // given
        Long parentCommentId = testRootComment.getId();
        Long cursor = null;
        int size = 5;

        // when
        CommentPageDTO result = commentService.getReplies(parentCommentId, cursor, size);

        // then
        assertThat(result.comments()).hasSize(1);
        assertThat(result.comments().get(0).content()).isEqualTo("답글 댓글");
        assertThat(result.comments().get(0).parentCommentId()).isEqualTo(testRootComment.getId());
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 존재하지_않는_부모_댓글의_답글을_조회하면_예외가_발생한다() {
        // given
        Long nonExistentParentCommentId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.getReplies(nonExistentParentCommentId, null, 5))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_루트_댓글을_생성할수있다() {
        // given
        Long postId = testPost.getId();
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("새로운 루트 댓글");

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when
        CommentResponseDTO result = commentService.createComment(postId, requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("새로운 루트 댓글");
        assertThat(result.parentCommentId()).isNull();
        assertThat(result.nickname()).isEqualTo("student");
    }

    @Test
    void 유저가_답글을_생성할수있다() {
        // given
        Long postId = testPost.getId();
        CommentRequestDTO requestDTO = CommentTestFixtures.createReplyCommentRequestDTO(testRootComment.getId(), "새로운 답글");

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when
        CommentResponseDTO result = commentService.createComment(postId, requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("새로운 답글");
        assertThat(result.parentCommentId()).isEqualTo(testRootComment.getId());
        assertThat(result.nickname()).isEqualTo("student");
    }

    @Test
    void 부모_댓글을_가지고_있는_답글의_답글을_생성하려하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        CommentRequestDTO requestDTO = CommentTestFixtures.createReplyCommentRequestDTO(testReplyComment.getId(), "답글의 답글");

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, requestDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 존재하지_않는_게시글에_댓글을_생성하면_예외가_발생한다() {
        // given
        Long nonExistentPostId = 999L;
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("댓글");

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(nonExistentPostId, requestDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 로그인하지_않은_상태로_댓글을_생성하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("댓글");

        given(userService.getCurrentUser()).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, requestDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인의_댓글_내용을_수정할수있다() {
        // given
        Long postId = testPost.getId();
        Long commentId = testRootComment.getId();
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("수정된 댓글");

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when
        commentService.updateComment(postId, commentId, requestDTO);

        // then
        Comment updatedComment = commentRepository.findById(commentId).orElseThrow();
        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    void 존재하지_않는_댓글을_수정하려하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        Long nonExistentCommentId = 999L;
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("수정된 댓글");

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(postId,nonExistentCommentId, requestDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 다른_유저의_댓글을_수정하려하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        Long commentId = testRootComment.getId();
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("수정된 댓글");

        given(userService.getCurrentUser()).willReturn(Optional.of(testOtherUser));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(postId, commentId, requestDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 로그인하지_않은_상태로_댓글을_수정하려하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        Long commentId = testRootComment.getId();
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("수정된 댓글");

        given(userService.getCurrentUser()).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(postId, commentId, requestDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저가_본인의_댓글을_삭제할수있다() {
        // given
        Long postId = testPost.getId();
        Long commentId = testRootComment.getId();

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when
        commentService.deleteComment(postId, commentId);

        // then
        Optional<Comment> deletedComment = commentRepository.findById(commentId);
        assertThat(deletedComment).isEmpty();
    }

    @Test
    void 존재하지_않는_댓글을_삭제하려하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        Long nonExistentCommentId = 999L;

        given(userService.getCurrentUser()).willReturn(Optional.of(testStudentUser));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(postId, nonExistentCommentId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 다른_유저의_댓글을_삭제하려하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        Long commentId = testRootComment.getId();

        given(userService.getCurrentUser()).willReturn(Optional.of(testOtherUser));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 로그인하지_않은_상태로_댓글을_삭제하려하면_예외가_발생한다() {
        // given
        Long postId = testPost.getId();
        Long commentId = testRootComment.getId();

        given(userService.getCurrentUser()).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                .isInstanceOf(CustomException.class);
    }
}