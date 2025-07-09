package kr.co.amateurs.server.controller.comment;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.comment.CommentTestFixtures;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.alarm.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

public class CommentControllerTest extends AbstractControllerTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private SseService sseService;

    private User guestUser;
    private User studentUser;
    private User adminUser;

    private Post communityPost;
    private Post togetherPost;
    private Post itPost;
    private Post projectPost;

    private String guestToken;
    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        cleanUpData();
        setUpData();
        doNothing().when(sseService).sendAlarmToUser(anyLong(), ArgumentMatchers.any(Alarm.class));
    }

    private void setUpData() {
        createUsers();
        createPosts();
        createTokens();
    }

    private void createUsers() {
        guestUser = userRepository.save(CommentTestFixtures.createGuestUser());
        studentUser = userRepository.save(CommentTestFixtures.createStudentUser());
        adminUser = userRepository.save(CommentTestFixtures.createAdminUser());
    }

    private void createPosts() {
        communityPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "커뮤니티 게시글", "내용", BoardType.FREE));

        togetherPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "함께해요 게시글", "내용", BoardType.MARKET));

        itPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "IT 게시글", "내용", BoardType.REVIEW));

        projectPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "프로젝트 게시글", "내용", BoardType.PROJECT_HUB));
    }

    private void createTokens() {
        guestToken = jwtProvider.generateAccessToken(guestUser.getEmail());
        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
    }

    private Comment createComment(Post post, String content, Comment parentComment, User user) {
        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .parentComment(parentComment)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .build();
        return commentRepository.save(comment);
    }

    private void cleanUpData() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class 익명_사용자는 {

        @Test
        void IT_게시판_댓글을_조회할_수_있다() {
            // given
            Comment rootComment = createComment(itPost, "IT 댓글", null, studentUser);

            // when & then
            given()
                    .when()
                    .get("/posts/{postId}/comments?size={size}", itPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1))
                    .body("comments[0].content", equalTo("IT 댓글"))
                    .body("comments[0].hasLiked", is(false));
        }

        @Test
        void PROJECT_게시판_댓글을_조회할_수_있다() {
            // given
            Comment rootComment = createComment(projectPost, "프로젝트 댓글", null, studentUser);

            // when & then
            given()
                    .when()
                    .get("/posts/{postId}/comments?size={size}", projectPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1))
                    .body("comments[0].content", equalTo("프로젝트 댓글"));
        }

        @Test
        void COMMUNITY_게시판_댓글을_조회할_수_없다() {
            // given
            Comment rootComment = createComment(communityPost, "커뮤니티 댓글", null, studentUser);

            // when & then
            given()
                    .when()
                    .get("/posts/{postId}/comments?size={size}", communityPost.getId(), 8)
                    .then()
                    .statusCode(403);
        }

        @Test
        void TOGETHER_게시판_댓글을_조회할_수_없다() {
            // given
            Comment rootComment = createComment(togetherPost, "함께해요 댓글", null, studentUser);

            // when & then
            given()
                    .when()
                    .get("/posts/{postId}/comments?size={size}", togetherPost.getId(), 8)
                    .then()
                    .statusCode(403);
        }

        @Test
        void 댓글을_작성할_수_없다() {
            // given
            CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("익명 댓글");

            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/posts/{postId}/comments", itPost.getId())
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class 게스트_사용자는 {

        @Test
        void COMMUNITY_게시판_댓글을_조회할_수_있다() {
            // given
            Comment rootComment = createComment(communityPost, "커뮤니티 댓글", null, studentUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", communityPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1))
                    .body("comments[0].content", equalTo("커뮤니티 댓글"));
        }

        @Test
        void IT_게시판_댓글을_조회할_수_있다() {
            // given
            Comment rootComment = createComment(itPost, "IT 댓글", null, studentUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", itPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1))
                    .body("comments[0].content", equalTo("IT 댓글"));
        }

        @Test
        void PROJECT_게시판_댓글을_조회할_수_있다() {
            // given
            Comment rootComment = createComment(projectPost, "프로젝트 댓글", null, studentUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", projectPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1))
                    .body("comments[0].content", equalTo("프로젝트 댓글"));
        }

        @Test
        void TOGETHER_게시판_댓글을_조회할_수_없다() {
            // given
            Comment rootComment = createComment(togetherPost, "함께해요 댓글", null, studentUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", togetherPost.getId(), 8)
                    .then()
                    .statusCode(403);
        }

        @Test
        void 댓글을_작성할_수_없다() {
            // given
            CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("게스트 댓글");

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/posts/{postId}/comments", communityPost.getId())
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    class 학생_사용자는 {

        @Test
        void 모든_게시판의_댓글을_조회할_수_있다() {
            // given
            Comment communityComment = createComment(communityPost, "커뮤니티 댓글", null, studentUser);
            Comment togetherComment = createComment(togetherPost, "함께해요 댓글", null, studentUser);
            Comment itComment = createComment(itPost, "IT 댓글", null, studentUser);
            Comment projectComment = createComment(projectPost, "프로젝트 댓글", null, studentUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", communityPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1));

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", togetherPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1));

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", itPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1));

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", projectPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1));
        }

        @Test
        void 모든_게시판에_댓글을_작성할_수_있다() {
            // given
            CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("학생 댓글");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/posts/{postId}/comments", communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("content", equalTo("학생 댓글"))
                    .body("nickname", equalTo(studentUser.getNickname()));
        }

        @Test
        void 본인_댓글을_수정할_수_있다() {
            // given
            Comment comment = createComment(communityPost, "원본 댓글", null, studentUser);
            CommentRequestDTO requestDTO = CommentTestFixtures.createCommentRequestDTO(null, "수정된 댓글");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/posts/{postId}/comments/{commentId}",
                            communityPost.getId(), comment.getId())
                    .then()
                    .statusCode(204);

            Comment updatedComment = commentRepository.findById(comment.getId()).orElse(null);
            assert updatedComment != null;
            assert updatedComment.getContent().equals("수정된 댓글");
        }

        @Test
        void 본인_댓글을_삭제할_수_있다() {
            // given
            Comment comment = createComment(communityPost, "삭제할 댓글", null, studentUser);
            Long commentId = comment.getId();

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/posts/{postId}/comments/{commentId}",
                            communityPost.getId(), commentId)
                    .then()
                    .statusCode(204);

            assert commentRepository.findById(commentId).isEmpty();
        }

        @Test
        void 다른_사용자의_댓글을_수정할_수_없다() {
            // given
            Comment adminComment = createComment(communityPost, "관리자 댓글", null, adminUser);
            CommentRequestDTO requestDTO = CommentTestFixtures.createCommentRequestDTO(null, "수정 시도");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/posts/{postId}/comments/{commentId}",
                            communityPost.getId(), adminComment.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 다른_사용자의_댓글을_삭제할_수_없다() {
            // given
            Comment adminComment = createComment(communityPost, "관리자 댓글", null, adminUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/posts/{postId}/comments/{commentId}",
                            communityPost.getId(), adminComment.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 답글을_작성할_수_있다() {
            // given
            Comment parentComment = createComment(communityPost, "부모 댓글", null, adminUser);
            CommentRequestDTO requestDTO = CommentTestFixtures.createReplyCommentRequestDTO(
                    parentComment.getId(), "답글");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/posts/{postId}/comments", communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("content", equalTo("답글"))
                    .body("parentCommentId", equalTo(parentComment.getId().intValue()));
        }

        @Test
        void 답글의_답글을_작성하려하면_400_에러가_발생한다() {
            // given
            Comment parentComment = createComment(communityPost, "부모 댓글", null, adminUser);
            Comment replyComment = createComment(communityPost, "답글", parentComment, studentUser);
            CommentRequestDTO requestDTO = CommentTestFixtures.createReplyCommentRequestDTO(
                    replyComment.getId(), "답글의 답글");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/posts/{postId}/comments", communityPost.getId())
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    class 관리자_사용자는 {

        @Test
        void 모든_게시판의_댓글을_조회할_수_있다() {
            // given
            Comment communityComment = createComment(communityPost, "커뮤니티 댓글", null, studentUser);
            Comment togetherComment = createComment(togetherPost, "함께해요 댓글", null, studentUser);
            Comment itComment = createComment(itPost, "IT 댓글", null, studentUser);
            Comment projectComment = createComment(projectPost, "프로젝트 댓글", null, studentUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", communityPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1));

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/posts/{postId}/comments?size={size}", togetherPost.getId(), 8)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1));
        }

        @Test
        void 모든_게시판에_댓글을_작성할_수_있다() {
            // given
            CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("관리자 댓글");

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/posts/{postId}/comments", togetherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("content", equalTo("관리자 댓글"))
                    .body("nickname", equalTo(adminUser.getNickname()));
        }

        @Test
        void 댓글을_수정할_수_있다() {
            // given
            Comment comment = createComment(communityPost, "관리자 원본 댓글", null, studentUser);
            CommentRequestDTO requestDTO = CommentTestFixtures.createCommentRequestDTO(null, "관리자 수정 댓글");

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/posts/{postId}/comments/{commentId}",
                            communityPost.getId(), comment.getId())
                    .then()
                    .statusCode(204);
        }

        @Test
        void 댓글을_삭제할_수_있다() {
            // given
            Comment comment = createComment(communityPost, "관리자가 삭제할 댓글", null, studentUser);
            Long commentId = comment.getId();

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .delete("/posts/{postId}/comments/{commentId}",
                            communityPost.getId(), commentId)
                    .then()
                    .statusCode(204);

            assert commentRepository.findById(commentId).isEmpty();
        }
    }

    @Nested
    class 공통_기능_테스트 {

        @Test
        void 댓글_목록을_커서_기반_페이징으로_조회할_수_있다() {
            // given
            createComment(itPost, "댓글 1", null, studentUser);
            createComment(itPost, "댓글 2", null, studentUser);
            createComment(itPost, "댓글 3", null, studentUser);

            // when
            Long nextCursor =
                    given()
                            .header("Authorization", "Bearer " + studentToken)
                            .when()
                            .get("/posts/{postId}/comments?size={size}", itPost.getId(), 2)
                            .then()
                            .statusCode(200)
                            .body("comments", hasSize(2))
                            .body("hasNext", is(true))
                            .body("nextCursor", notNullValue())
                            .extract()
                            .jsonPath()
                            .getLong("nextCursor");

            // when
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments?cursor={cursor}&size={size}",
                            itPost.getId(), nextCursor, 2)
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(1))
                    .body("hasNext", is(false))
                    .body("nextCursor", nullValue());
        }

        @Test
        void 특정_댓글의_답글_목록을_조회할_수_있다() {
            // given
            Comment parentComment = createComment(itPost, "부모 댓글", null, studentUser);
            createComment(itPost, "첫 번째 답글", parentComment, studentUser);
            createComment(itPost, "두 번째 답글", parentComment, adminUser);

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments/{commentId}/replies",
                            itPost.getId(), parentComment.getId())
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(2))
                    .body("hasNext", is(false))
                    .body("comments[0].content", equalTo("첫 번째 답글"))
                    .body("comments[0].parentCommentId", equalTo(parentComment.getId().intValue()));
        }

        @Test
        void 댓글이_없는_게시글에_접근하면_빈_댓글_목록이_반환된다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments", itPost.getId())
                    .then()
                    .statusCode(200)
                    .body("comments", hasSize(0))
                    .body("hasNext", is(false))
                    .body("nextCursor", nullValue());
        }

        @Test
        void 존재하지_않는_게시글의_댓글을_조회하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/posts/{postId}/comments", 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 존재하지_않는_댓글을_수정하려하면_404_에러가_발생한다() {
            // given
            CommentRequestDTO requestDTO = CommentTestFixtures.createCommentRequestDTO(null, "수정 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/posts/{postId}/comments/{commentId}",
                            itPost.getId(), 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 존재하지_않는_댓글을_삭제하려하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/posts/{postId}/comments/{commentId}",
                            itPost.getId(), 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 빈_내용으로_댓글을_생성하면_400_에러가_발생한다() {
            // given
            CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/posts/{postId}/comments", itPost.getId())
                    .then()
                    .statusCode(400);
        }
    }

    private void setUpData() {
        createUsers();
        createPosts();
        createTokens();
    }

    private void createUsers() {
        guestUser = userRepository.save(CommentTestFixtures.createGuestUser());
        studentUser = userRepository.save(CommentTestFixtures.createStudentUser());
        adminUser = userRepository.save(CommentTestFixtures.createAdminUser());
    }

    private void createPosts() {
        communityPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "커뮤니티 게시글", "내용", BoardType.FREE));

        togetherPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "함께해요 게시글", "내용", BoardType.MARKET));

        itPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "IT 게시글", "내용", BoardType.REVIEW));

        projectPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "프로젝트 게시글", "내용", BoardType.PROJECT_HUB));
    }

    private void createTokens() {
        guestToken = jwtProvider.generateAccessToken(guestUser.getEmail());
        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
    }

    private Comment createComment(Post post, String content, Comment parentComment, User user) {
        Long parentId = null;
        if (parentComment != null) {
            parentId = parentComment.getId();
        }

        Comment comment = Comment.builder()
                .user(user)
                .postId(post.getId())
                .parentCommentId(parentId)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .build();
        return commentRepository.save(comment);
    }

    private void cleanUpData() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}