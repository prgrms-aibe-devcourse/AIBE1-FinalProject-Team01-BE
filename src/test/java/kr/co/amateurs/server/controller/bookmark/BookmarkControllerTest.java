package kr.co.amateurs.server.controller.bookmark;

import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.comment.CommentTestFixtures;
import kr.co.amateurs.server.fixture.community.CommunityTestFixtures;
import kr.co.amateurs.server.fixture.project.BookmarkFixture;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BookmarkControllerTest extends AbstractControllerTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PostStatisticsRepository postStatisticsRepository;

    private User guestUser;
    private User studentUser;
    private User adminUser;
    private User anotherUser;

    private Post communityPost;
    private Post togetherPost;
    private Post itPost;
    private Post projectPost;

    // 토큰들
    private String guestToken;
    private String studentToken;
    private String adminToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        cleanUpData();
        setUpData();
    }

    @Nested
    class 익명_사용자는 {

        @Test
        void IT_게시판_게시글을_북마크할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/bookmarks/{postId}",  itPost.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void PROJECT_게시판_게시글을_북마크할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/bookmarks/{postId}",  projectPost.getId())
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    class 게스트_사용자는 {

        @Test
        void COMMUNITY_게시판_게시글을_북마크할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/bookmarks/{postId}",  communityPost.getId())
                    .then()
                    .statusCode(201);
        }

        @Test
        void IT_게시판_게시글을_북마크할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/bookmarks/{postId}", itPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(itPost.getId().intValue()));
        }

        @Test
        void PROJECT_게시판_게시글을_북마크할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/bookmarks/{postId}",  projectPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(projectPost.getId().intValue()));
        }

        @Test
        void TOGETHER_게시판_게시글을_북마크할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/bookmarks/{postId}",  togetherPost.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 북마크한_게시글의_북마크를_제거할_수_있다() {
            // given
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/bookmarks/{postId}",  communityPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .delete("/bookmarks/{postId}",  communityPost.getId())
                    .then()
                    .statusCode(204);
        }
    }

    @Nested
    class 학생_사용자는 {

        @Test
        void 모든_게시판의_게시글을_북마크할_수_있다() {
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/bookmarks/{postId}", communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(communityPost.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/bookmarks/{postId}", togetherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(togetherPost.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/bookmarks/{postId}", itPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(itPost.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/bookmarks/{postId}", projectPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(projectPost.getId().intValue()));
        }

        @Test
        void 같은_게시글을_중복_북마크할_수_없다() {
            // given
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/bookmarks/{postId}", communityPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/bookmarks/{postId}", communityPost.getId())
                    .then()
                    .statusCode(409);
        }

        @Test
        void 북마크하지_않은_게시글의_북마크를_제거하려해도_에러가_발생하지_않는다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/bookmarks/{postId}", communityPost.getId())
                    .then()
                    .statusCode(204);
        }
    }

    @Nested
    class 관리자_사용자는 {

        @Test
        void 모든_게시판의_게시글을_북마크할_수_있다() {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/bookmarks/{postId}", communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(communityPost.getId().intValue()));


            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/bookmarks/{postId}", togetherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(togetherPost.getId().intValue()));
        }

        @Test
        void 북마크한_게시글의_북마크를_제거할_수_있다() {
            // given
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/bookmarks/{postId}", togetherPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .delete("/bookmarks/{postId}", togetherPost.getId())
                    .then()
                    .statusCode(204);
        }
    }

    @Nested
    class 공통_에러_처리_테스트 {

        @Test
        void 존재하지_않는_게시글을_북마크하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/bookmarks/{postId}", 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 존재하지_않는_게시글의_북마크를_제거하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/bookmarks/{postId}", 999L)
                    .then()
                    .statusCode(404);
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
        anotherUser = userRepository.save(CommentTestFixtures.createAnotherUser());
    }

    private void createPosts() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        communityPost = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    CommunityTestFixtures.createPost(studentUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.FREE));

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });

        togetherPost = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    CommentTestFixtures.createCustomPost(studentUser, "함께해요 게시글", "내용", BoardType.MARKET));

            marketRepository.save(CommentTestFixtures.createMarketItem(post));
            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });

        itPost = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    CommentTestFixtures.createCustomPost(studentUser, "IT 게시글", "내용", BoardType.REVIEW));

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });

        projectPost = transactionTemplate.execute(status -> {
            Post post = postRepository.save(
                    CommentTestFixtures.createCustomPost(studentUser, "프로젝트 게시글", "내용", BoardType.PROJECT_HUB));

            PostStatistics postStatistics = PostStatistics.from(post);
            postStatisticsRepository.save(postStatistics);

            return post;
        });
    }

    private void createTokens() {
        guestToken = jwtProvider.generateAccessToken(guestUser.getEmail());
        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
        anotherUserToken = jwtProvider.generateAccessToken(anotherUser.getEmail());
    }

    private void cleanUpData() {
        postStatisticsRepository.deleteAll();
        bookmarkRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}