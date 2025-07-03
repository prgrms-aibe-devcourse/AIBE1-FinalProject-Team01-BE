package kr.co.amateurs.server.controller.like;

import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.comment.CommentTestFixtures;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LikeControllerTest extends AbstractControllerTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private User guestUser;
    private User studentUser;
    private User adminUser;

    private Post communityPost;
    private Post togetherPost;
    private Post itPost;
    private Post projectPost;

    private Comment communityComment;
    private Comment togetherComment;
    private Comment itComment;
    private Comment projectComment;

    private String guestToken;
    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        cleanUpData();
        setUpData();
    }

    @Nested
    class 익명_사용자는 {

        @Test
        void IT_게시판_게시글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/posts/{postId}/likes", itPost.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void PROJECT_게시판_게시글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/posts/{postId}/likes", projectPost.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void IT_게시판_댓글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            itPost.getId(), itComment.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void PROJECT_게시판_댓글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            projectPost.getId(), projectComment.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void COMMUNITY_게시판_게시글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void TOGETHER_게시판_게시글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/posts/{postId}/likes", togetherPost.getId())
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class 게스트_사용자는 {

        @Test
        void COMMUNITY_게시판_게시글에_좋아요를_할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"))
                    .body("id", equalTo(communityPost.getId().intValue()));
        }

        @Test
        void IT_게시판_게시글에_좋아요를_할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/likes", itPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"))
                    .body("id", equalTo(itPost.getId().intValue()));
        }

        @Test
        void PROJECT_게시판_게시글에_좋아요를_할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/likes", projectPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"))
                    .body("id", equalTo(projectPost.getId().intValue()));
        }

        @Test
        void TOGETHER_게시판_게시글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/likes", togetherPost.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void COMMUNITY_게시판_댓글에_좋아요를_할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            communityPost.getId(), communityComment.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("comment"))
                    .body("id", equalTo(communityComment.getId().intValue()));
        }

        @Test
        void TOGETHER_게시판_댓글에_좋아요를_할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            togetherPost.getId(), togetherComment.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 좋아요한_게시글의_좋아요를_제거할_수_있다() {
            // given
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .delete("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(204);
        }

        @Test
        void 좋아요한_댓글의_좋아요를_제거할_수_있다() {
            // given
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            itPost.getId(), itComment.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .delete("/posts/{postId}/comments/{commentId}/likes",
                            itPost.getId(), itComment.getId())
                    .then()
                    .statusCode(204);
        }
    }

    @Nested
    class 학생_사용자는 {

        @Test
        void 모든_게시판의_게시글에_좋아요를_할_수_있다() {
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/likes", togetherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/likes", itPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/likes", projectPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"));
        }

        @Test
        void 모든_게시판의_댓글에_좋아요를_할_수_있다() {
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            communityPost.getId(), communityComment.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("comment"));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            togetherPost.getId(), togetherComment.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("comment"));
        }

        @Test
        void 같은_게시글에_중복_좋아요를_할_수_없다() {
            // given
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(409);
        }

        @Test
        void 같은_댓글에_중복_좋아요를_할_수_없다() {
            // given
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            communityPost.getId(), communityComment.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            communityPost.getId(), communityComment.getId())
                    .then()
                    .statusCode(409);
        }

        @Test
        void 좋아요하지_않은_게시글의_좋아요를_제거하려하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(404);
        }

        @Test
        void 좋아요하지_않은_댓글의_좋아요를_제거하려하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/posts/{postId}/comments/{commentId}/likes",
                            communityPost.getId(), communityComment.getId())
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    class 관리자_사용자는 {

        @Test
        void 모든_게시판의_게시글에_좋아요를_할_수_있다() {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/posts/{postId}/likes", communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"))
                    .body("id", equalTo(communityPost.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/posts/{postId}/likes", togetherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("post"))
                    .body("id", equalTo(togetherPost.getId().intValue()));
        }

        @Test
        void 모든_게시판의_댓글에_좋아요를_할_수_있다() {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            itPost.getId(), itComment.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("comment"))
                    .body("id", equalTo(itComment.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            projectPost.getId(), projectComment.getId())
                    .then()
                    .statusCode(201)
                    .body("targetType", equalTo("comment"))
                    .body("id", equalTo(projectComment.getId().intValue()));
        }

        @Test
        void 좋아요한_게시글과_댓글의_좋아요를_제거할_수_있다() {
            // given
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/posts/{postId}/likes", togetherPost.getId())
                    .then()
                    .statusCode(201);

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            togetherPost.getId(), togetherComment.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .delete("/posts/{postId}/likes", togetherPost.getId())
                    .then()
                    .statusCode(204);

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .delete("/posts/{postId}/comments/{commentId}/likes",
                            togetherPost.getId(), togetherComment.getId())
                    .then()
                    .statusCode(204);
        }
    }

    @Nested
    class 공통_에러_처리_테스트 {

        @Test
        void 존재하지_않는_게시글에_좋아요를_하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/likes", 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 존재하지_않는_댓글에_좋아요를_하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            communityPost.getId(), 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 존재하지_않는_게시글의_좋아요를_제거하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/posts/{postId}/likes", 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 다른_게시글의_댓글에_좋아요를_하면_400_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/posts/{postId}/comments/{commentId}/likes",
                            communityPost.getId(), togetherComment.getId()) // 다른 게시글의 댓글
                    .then()
                    .statusCode(400);
        }
    }

    private void setUpData() {
        createUsers();
        createPosts();
        createComments();
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

    private void createComments() {
        communityComment = commentRepository.save(
                CommentTestFixtures.createComment(communityPost, studentUser, null, "커뮤니티 댓글"));
        togetherComment = commentRepository.save(
                CommentTestFixtures.createComment(togetherPost, studentUser, null, "함께해요 댓글"));
        itComment = commentRepository.save(
                CommentTestFixtures.createComment(itPost, studentUser, null, "IT 댓글"));
        projectComment = commentRepository.save(
                CommentTestFixtures.createComment(projectPost, studentUser, null, "프로젝트 댓글"));
    }

    private void createTokens() {
        guestToken = jwtProvider.generateAccessToken(guestUser.getEmail());
        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
    }

    private void cleanUpData() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}