package kr.co.amateurs.server.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.TestSecurityConfig;
import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import io.restassured.http.ContentType;
import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.service.comment.CommentService;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.fixture.comment.CommentTestFixtures;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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

    private User testUser;
    private User anotherUser;
    private Post testPost;
    private Post testITPost;
    private String userToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(CommentTestFixtures.createStudentUser());
        anotherUser = userRepository.save(CommentTestFixtures.createAnotherUser());

        testPost = postRepository.save(CommentTestFixtures.createTestPost(testUser));
        testITPost = postRepository.save(CommentTestFixtures.createCustomPost(testUser,"IT게시글","IT내용", BoardType.REVIEW));

        userToken = jwtProvider.generateAccessToken(testUser.getEmail());
        anotherUserToken = jwtProvider.generateAccessToken(anotherUser.getEmail());
    }

    @Test
    void 게시글의_댓글_목록을_조회할_수_있다() {
        // given
        Comment rootComment1 = createComment("첫 번째 댓글", null, testUser);
        Comment rootComment2 = createComment("두 번째 댓글", null, anotherUser);

        createComment("첫 번째 답글", rootComment1, testUser);
        createComment("두 번째 답글", rootComment1, anotherUser);

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/posts/{postId}/comments?size={size}", testPost.getId(), 8)
                .then()
                .statusCode(200)
                .body("comments", hasSize(2))
                .body("hasNext", is(false))
                .body("nextCursor", nullValue())
                .body("comments[0].content", equalTo("첫 번째 댓글"))
                .body("comments[0].replyCount", equalTo(2))
                .body("comments[0].likeCount", equalTo(0))
                .body("comments[0].hasLiked", is(false))
                .body("comments[1].content", equalTo("두 번째 댓글"))
                .body("comments[1].replyCount", equalTo(0))
                .body("comments[1].likeCount", equalTo(0))
                .body("comments[1].hasLiked", is(false));
    }

    @Test
    void 로그인하지_않은_사용자는_IT_게시판_댓글을_조회할_수_있다() {
        // given
        Comment rootComment = createComment(testITPost,"테스트 댓글", null, testUser);

        // when & then
        given()
                .when()
                .get("/posts/{postId}/comments?size={size}", testITPost.getId(), 8)
                .then()
                .statusCode(200)
                .body("comments", hasSize(1))
                .body("comments[0].likeCount", equalTo(5))
                .body("comments[0].hasLiked", is(false));
    }

    @Test
    void 로그인하지_않은_사용자는_권한이_없는_게시판_댓글을_조회할_수_없다() {
        // given
        Comment rootComment = createComment("테스트 댓글", null, testUser);

        // when & then
        given()
                .when()
                .get("/posts/{postId}/comments?size={size}", testPost.getId(), 8)
                .then()
                .statusCode(403);
    }

    @Test
    void 커서_기반_페이징이_정상_동작한다() {
        // given
        createComment("댓글 1", null, testUser);
        createComment("댓글 2", null, testUser);
        createComment("댓글 3", null, testUser);

        // when
        Long nextCursor =
                given()
                        .header("Authorization", "Bearer " + userToken)
                        .when()
                        .get("/posts/{postId}/comments?size={size}", testPost.getId(), 2)
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
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/posts/{postId}/comments?cursor={cursor}&size={size}",
                        testPost.getId(), nextCursor, 2)
                .then()
                .statusCode(200)
                .body("comments", hasSize(1))
                .body("hasNext", is(false))
                .body("nextCursor", nullValue());
    }

    @Test
    void 특정_댓글의_답글_목록을_조회할_수_있다() {
        // given
        Comment parentComment = createComment("부모 댓글", null, testUser);
        Comment reply1 = createComment("답글", parentComment, testUser);
        createComment("두 번째 답글", parentComment, anotherUser);

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/posts/{postId}/comments/{commentId}/replies",
                        testPost.getId(), parentComment.getId())
                .then()
                .statusCode(200)
                .body("comments", hasSize(2))
                .body("hasNext", is(false))
                .body("comments[0].content", equalTo("답글"))
                .body("comments[0].parentCommentId", equalTo(parentComment.getId().intValue()))
                .body("comments[0].replyCount", equalTo(0))
                .body("comments[0].likeCount", equalTo(0))
                .body("comments[0].hasLiked", is(false));
    }

    @Test
    void 댓글이_없는_게시글에_접근하면_빈_댓글_목록이_반환된다() {
        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/posts/{postId}/comments", testPost.getId())
                .then()
                .statusCode(200)
                .body("comments", hasSize(0))
                .body("hasNext", is(false))
                .body("nextCursor", nullValue());
    }

    @Test
    void 유저가_댓글을_생성할_수_있다() {
        // given
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("새로운 루트 댓글");

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/posts/{postId}/comments", testPost.getId())
                .then()
                .statusCode(201)
                .body("content", equalTo("새로운 루트 댓글"))
                .body("parentCommentId", nullValue())
                .body("nickname", equalTo(testUser.getNickname()))
                .body("likeCount", equalTo(0))
                .body("hasLiked", is(false));
    }

    @Test
    void 유저가_답글을_생성할_수_있다() {
        // given
        Comment parentComment = createComment("부모 댓글", null, testUser);
        CommentRequestDTO requestDTO = CommentTestFixtures.createReplyCommentRequestDTO(
                parentComment.getId(), "새로운 답글");

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/posts/{postId}/comments", testPost.getId())
                .then()
                .statusCode(201)
                .body("content", equalTo("새로운 답글"))
                .body("parentCommentId", equalTo(parentComment.getId().intValue()))
                .body("nickname", equalTo(testUser.getNickname()));
    }

    @Test
    void 답글의_답글을_생성하려하면_400_에러가_발생한다() {
        // given
        Comment parentComment = createComment("부모 댓글", null, testUser);
        Comment replyComment = createComment("답글", parentComment, testUser);
        CommentRequestDTO requestDTO = CommentTestFixtures.createReplyCommentRequestDTO(
                replyComment.getId(), "답글의 답글");

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/posts/{postId}/comments", testPost.getId())
                .then()
                .statusCode(400);
    }

    @Test
    void 로그인하지_않으면_댓글을_생성할_수_없다() {
        // given
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("새로운 댓글");

        // when & then
        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/posts/{postId}/comments", testPost.getId())
                .then()
                .statusCode(403);
    }

    @Test
    void 본인_댓글을_수정할_수_있다() {
        // given
        Comment comment = createComment("원본 댓글", null, testUser);
        CommentRequestDTO requestDTO = CommentTestFixtures.createCommentRequestDTO(
                null, "수정 댓글");

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/posts/{postId}/comments/{commentId}",
                        testPost.getId(), comment.getId())
                .then()
                .statusCode(204);

        Comment updatedComment = commentRepository.findById(comment.getId()).orElse(null);
        assert updatedComment != null;
        assert updatedComment.getContent().equals("수정 댓글");
    }

    @Test
    void 다른_사용자의_댓글은_수정할_수_없다() {
        // given
        Comment comment = createComment("다른 사용자 댓글", null, anotherUser);
        CommentRequestDTO requestDTO = CommentTestFixtures.createCommentRequestDTO(null, "수정 시도");

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/posts/{postId}/comments/{commentId}",
                        testPost.getId(), comment.getId())
                .then()
                .statusCode(403);
    }

    @Test
    void 본인_댓글을_삭제할_수_있다() {
        // given
        Comment comment = createComment("삭제할 댓글", null, testUser);
        Long commentId = comment.getId();

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/posts/{postId}/comments/{commentId}",
                        testPost.getId(), commentId)
                .then()
                .statusCode(204);

        // 실제로 삭제되었는지 확인
        assert commentRepository.findById(commentId).isEmpty();
    }

    @Test
    void 다른_사용자의_댓글은_삭제할_수_없다() {
        // given
        Comment comment = createComment("다른 사용자 댓글", null, anotherUser);

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/posts/{postId}/comments/{commentId}",
                        testPost.getId(), comment.getId())
                .then()
                .statusCode(403);
    }

    @Test
    void 존재하지_않는_게시글의_댓글을_조회하면_404_에러가_발생한다() {
        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .put("/posts/{postId}/comments/{commentId}",
                        testPost.getId(), 999L)
                .then()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_댓글을_삭제하려하면_404_에러가_발생한다() {
        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/posts/{postId}/comments/{commentId}",
                        testPost.getId(), 999L)
                .then()
                .statusCode(404);
    }

    @Test
    void 빈_내용으로_댓글을_생성하면_400_에러가_발생한다() {
        // given
        CommentRequestDTO requestDTO = CommentTestFixtures.createRootCommentRequestDTO("");

        // when & then
        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/posts/{postId}/comments", testPost.getId())
                .then()
                .statusCode(400);
    }



    private Comment createComment(String content, Comment parentComment, User user) {
        Comment comment = Comment.builder()
                .user(user)
                .post(testPost)
                .parentComment(parentComment)
                .content(content)
                .likeCount(0)
                .isDeleted(false)
                .build();
        return commentRepository.save(comment);
    }

    private Comment createComment(Post post,String content, Comment parentComment, User user) {
        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .parentComment(parentComment)
                .content(content)
                .likeCount(5)
                .isDeleted(false)
                .build();
        return commentRepository.save(comment);
    }
}
