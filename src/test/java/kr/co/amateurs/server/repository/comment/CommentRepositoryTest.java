package kr.co.amateurs.server.repository.comment;

import kr.co.amateurs.server.domain.dto.comment.ReplyCount;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CommentRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("테스트유저")
                .role(Role.STUDENT)
                .build();
        entityManager.persistAndFlush(testUser);

        testPost = Post.builder()
                .user(testUser)
                .title("테스트 제목")
                .content("테스트 내용")
                .boardType(BoardType.FREE)
                .build();
        entityManager.persistAndFlush(testPost);
    }

    @Test
    void 게시글을_상세_조회하면_처음에는_루트_댓글만_pageSize만큼_반환_되어야_한다(){
        //Given
        Comment comment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글1")
                .build();
        entityManager.persistAndFlush(comment);

        Comment commentReply = Comment.builder()
                .post(testPost)
                .user(testUser)
                .parentComment(comment)
                .content("테스트 답글1")
                .build();
        entityManager.persistAndFlush(commentReply);

        //When
        List<Comment> result = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(
                testPost,
                PageRequest.of(0, 10)
        );

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 댓글1");
    }

    @Test
    void 주어진_게시글에_특정_ID_이후의_최상위_댓글_조회시_다음_댓글이_반환된다(){
        //Given
        Comment comment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글1")
                .build();
        entityManager.persistAndFlush(comment);

        Comment comment2 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글2")
                .build();
        entityManager.persistAndFlush(comment2);

        //When
        List<Comment> result = commentRepository.findByPostAndParentCommentIsNullAndIdGreaterThanOrderByCreatedAtAsc(
                testPost,
                comment.getId(),
                PageRequest.of(0, 1)
        );

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 댓글2");
    }

    @Test
    void 주어진_게시글에서_부모_댓글을_가지는_답글만_반환_되어야_한다(){
        //Given
        Comment comment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글1")
                .build();
        entityManager.persistAndFlush(comment);

        Comment commentReply = Comment.builder()
                .post(testPost)
                .user(testUser)
                .parentComment(comment)
                .content("테스트 답글1")
                .build();
        entityManager.persistAndFlush(commentReply);

        //When
        List<Comment> result = commentRepository.findByParentCommentOrderByCreatedAtAsc(
                comment,
                PageRequest.of(0, 10)
        );

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 답글1");
    }

    @Test
    void 주어진_게시글에서_특정_ID_이후의_최상위_답글_조회시_다음_답글이_반환_되어야_한다(){
        //Given
        Comment comment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("테스트 댓글1")
                .build();
        entityManager.persistAndFlush(comment);

        Comment commentReply = Comment.builder()
                .post(testPost)
                .user(testUser)
                .parentComment(comment)
                .content("테스트 답글1")
                .build();
        entityManager.persistAndFlush(commentReply);

        Comment commentReply2 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .parentComment(comment)
                .content("테스트 답글2")
                .build();
        entityManager.persistAndFlush(commentReply2);

        //When
        List<Comment> result = commentRepository.findByParentCommentAndIdGreaterThanOrderByCreatedAtAsc(
                comment,
                commentReply.getId(),
                PageRequest.of(0, 1)
        );

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 답글2");
    }

    @Test
    void 주어진_부모댓글들에_대해_각각의_답글_개수를_조회하면_개수가_반환된다() {
        //Given
        Comment parentComment1 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("부모댓글1")
                .build();
        entityManager.persistAndFlush(parentComment1);

        Comment parentComment2 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("부모댓글2")
                .build();
        entityManager.persistAndFlush(parentComment2);

        Comment parentComment3 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content("부모댓글3")
                .build();
        entityManager.persistAndFlush(parentComment3);

        Comment reply1_1 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .parentComment(parentComment1)
                .content("답글1-1")
                .build();
        entityManager.persistAndFlush(reply1_1);

        Comment reply1_2 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .parentComment(parentComment1)
                .content("답글1-2")
                .build();
        entityManager.persistAndFlush(reply1_2);

        Comment reply2_1 = Comment.builder()
                .post(testPost)
                .user(testUser)
                .parentComment(parentComment2)
                .content("답글2-1")
                .build();
        entityManager.persistAndFlush(reply2_1);

        //When
        List<Long> parentIds = List.of(
                parentComment1.getId(),
                parentComment2.getId(),
                parentComment3.getId()
        );
        List<ReplyCount> result = commentRepository.countRepliesByParentIds(parentIds);

        //Then
        assertThat(result).hasSize(2);

        Map<Long, Long> replyCountMap = result.stream()
                .filter(rc -> rc.getParentCommentId()!=null && rc.getCount() != null)
                .collect(Collectors.toMap(
                        ReplyCount::getParentCommentId,
                        ReplyCount::getCount
                ));
                ;

        assertThat(replyCountMap.get(parentComment1.getId())).isEqualTo(2L);
        assertThat(replyCountMap.get(parentComment2.getId())).isEqualTo(1L);
        assertThat(replyCountMap.containsKey(parentComment3.getId())).isFalse();
    }

    @Test
    void 댓글이_없는_게시글로_조회시_빈_리스트가_반환된다() {
        // When
        List<Comment> result = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(
                testPost,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result).isEmpty();
    }





}
