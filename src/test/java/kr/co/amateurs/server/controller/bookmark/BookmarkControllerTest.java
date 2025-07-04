package kr.co.amateurs.server.controller.bookmark;

import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.comment.CommentTestFixtures;
import kr.co.amateurs.server.fixture.project.BookmarkFixture;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), itPost.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void PROJECT_게시판_게시글을_북마크할_수_없다() {
            // when & then
            given()
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), projectPost.getId())
                    .then()
                    .statusCode(401);
        }

        @Test
        void 북마크_목록을_조회할_수_없다() {
            // when & then
            given()
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
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
                    .post("/users/{userId}/bookmarks/{postId}", guestUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(communityPost.getId().intValue()))
                    .body("boardType", equalTo(BoardType.FREE.toString()))
                    .body("title", equalTo("커뮤니티 게시글"));
        }

        @Test
        void IT_게시판_게시글을_북마크할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", guestUser.getId(), itPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(itPost.getId().intValue()))
                    .body("boardType", equalTo(BoardType.REVIEW.toString()));
        }

        @Test
        void PROJECT_게시판_게시글을_북마크할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", guestUser.getId(), projectPost.getId())
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
                    .post("/users/{userId}/bookmarks/{postId}", guestUser.getId(), togetherPost.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 본인의_북마크_목록을_조회할_수_있다() {
            // given
            bookmarkRepository.save(BookmarkFixture.createBookmark(guestUser, communityPost));
            bookmarkRepository.save(BookmarkFixture.createBookmark(guestUser, itPost));

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .get("/users/{userId}/bookmarks", guestUser.getId())
                    .then()
                    .statusCode(200)
                    .body("pageInfo.totalElements", equalTo(2));
        }

        @Test
        void 다른_사용자의_북마크_목록을_조회할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 다른_사용자_계정으로_북마크를_추가할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 북마크한_게시글의_북마크를_제거할_수_있다() {
            // given
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", guestUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .delete("/users/{userId}/bookmarks/{postId}", guestUser.getId(), communityPost.getId())
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
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(communityPost.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), togetherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(togetherPost.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), itPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(itPost.getId().intValue()));

            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), projectPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(projectPost.getId().intValue()));
        }

        @Test
        void 본인의_북마크_목록을_조회할_수_있다() {
            // given
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, communityPost));
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, togetherPost));
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, itPost));

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
                    .then()
                    .statusCode(200)
                    .body("pageInfo.totalElements", equalTo(3));
        }

        @Test
        void 북마크_목록을_페이징으로_조회할_수_있다() {
            // given
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, communityPost));
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, togetherPost));
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, itPost));
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, projectPost));

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("page", 0)
                    .param("size", 2)
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
                    .then()
                    .statusCode(200)
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(2))
                    .body("pageInfo.totalElements", equalTo(4))
                    .body("pageInfo.totalPages", equalTo(2));

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("page", 1)
                    .param("size", 2)
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
                    .then()
                    .statusCode(200)
                    .body("pageInfo.pageNumber", equalTo(1))
                    .body("pageInfo.pageSize", equalTo(2));
        }

        @Test
        void 같은_게시글을_중복_북마크할_수_없다() {
            // given
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(409);
        }

        @Test
        void 북마크하지_않은_게시글의_북마크를_제거하려해도_에러가_발생하지_않는다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/users/{userId}/bookmarks/{postId}", studentUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(204);
        }

        @Test
        void 다른_사용자의_북마크_목록을_조회할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/users/{userId}/bookmarks", guestUser.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 다른_사용자_계정으로_북마크를_추가할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", guestUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    class 관리자_사용자는 {

        @Test
        void 모든_게시판의_게시글을_북마크할_수_있다() {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", adminUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(communityPost.getId().intValue()));


            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", adminUser.getId(), togetherPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(togetherPost.getId().intValue()));
        }

        @Test
        void 본인의_북마크_목록을_조회할_수_있다() {
            // given
            bookmarkRepository.save(BookmarkFixture.createBookmark(adminUser, communityPost));
            bookmarkRepository.save(BookmarkFixture.createBookmark(adminUser, togetherPost));

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/users/{userId}/bookmarks", adminUser.getId())
                    .then()
                    .statusCode(200)
                    .body("pageInfo.totalElements", equalTo(2));
        }

        @Test
        void 다른_사용자의_북마크_목록을_조회할_수_있다() {
            // given
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, communityPost));

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
                    .then()
                    .statusCode(200)
                    .body("pageInfo.totalElements", equalTo(1));
        }

        @Test
        void 다른_사용자_계정으로도_북마크를_추가할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(201)
                    .body("postId", equalTo(communityPost.getId().intValue()));
        }

        @Test
        void 다른_사용자의_북마크를_제거할_수_있다() {
            // given
            bookmarkRepository.save(BookmarkFixture.createBookmark(studentUser, communityPost));

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .delete("/users/{userId}/bookmarks/{postId}", studentUser.getId(), communityPost.getId())
                    .then()
                    .statusCode(204);
        }

        @Test
        void 북마크한_게시글의_북마크를_제거할_수_있다() {
            // given
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", adminUser.getId(), togetherPost.getId())
                    .then()
                    .statusCode(201);

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .delete("/users/{userId}/bookmarks/{postId}", adminUser.getId(), togetherPost.getId())
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
                    .post("/users/{userId}/bookmarks/{postId}", studentUser.getId(), 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 잘못된_userId_로_북마크하면_403_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .post("/users/{userId}/bookmarks/{postId}", 999L, communityPost.getId())
                    .then()
                    .statusCode(403);
        }

        @Test
        void 존재하지_않는_게시글의_북마크를_제거하면_404_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/users/{userId}/bookmarks/{postId}", studentUser.getId(), 999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        void 잘못된_userId로_북마크_목록을_조회하면_403_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/users/{userId}/bookmarks", 999L)
                    .then()
                    .statusCode(403);
        }

        @Test
        void 음수_페이지_번호로_북마크_목록을_조회하면_400_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("page", -1)
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
                    .then()
                    .statusCode(400);
        }

        @Test
        void 잘못된_페이지_크기로_북마크_목록을_조회하면_400_에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("size", 0)
                    .when()
                    .get("/users/{userId}/bookmarks", studentUser.getId())
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
        anotherUser = userRepository.save(CommentTestFixtures.createAnotherUser());
    }

    private void createPosts() {
        communityPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "커뮤니티 게시글", "내용", BoardType.FREE));

        togetherPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "함께해요 게시글", "내용", BoardType.MARKET));

        marketRepository.save(CommentTestFixtures.createMarketItem(togetherPost));

        itPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "IT 게시글", "내용", BoardType.REVIEW));

        projectPost = postRepository.save(
                CommentTestFixtures.createCustomPost(studentUser, "프로젝트 게시글", "내용", BoardType.PROJECT_HUB));
    }

    private void createTokens() {
        guestToken = jwtProvider.generateAccessToken(guestUser.getEmail());
        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
        anotherUserToken = jwtProvider.generateAccessToken(anotherUser.getEmail());
    }

    private void cleanUpData() {
        bookmarkRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}