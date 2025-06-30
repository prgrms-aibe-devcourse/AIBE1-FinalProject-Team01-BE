package kr.co.amateurs.server.controller.it;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.it.ITTestFixtures;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ITControllerTest extends AbstractControllerTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private User testUser;
    private User anotherUser;
    private Post testPost;
    private String userToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        cleanUpData();
        setupTestData();
    }

    @Nested
    class 게시글_조회_테스트 {

        @Test
        void 유저가_키워드없이_검색하면_전체_게시글_목록이_반환되어야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .param("page", "0")
                    .param("sortType", "LATEST")
                    .param("pageSize", "8")
                    .when()
                    .get("/IT/{boardType}", BoardType.REVIEW)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("totalPages", greaterThanOrEqualTo(0))
                    .body("number", equalTo(0))
                    .body("size", equalTo(8));
        }

        @Test
        void 유저가_키워드로_검색하면_해당_게시글_목록이_반환되어야_한다() {
            // given
            String keyword = "테스트";

            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .param("keyword", keyword)
                    .param("page", "0")
                    .param("sortType", "LATEST")
                    .param("pageSize", "8")
                    .when()
                    .get("/IT/{boardType}", BoardType.REVIEW)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("number", equalTo(0))
                    .body("size", lessThanOrEqualTo(8));
        }

        @Test
        void 유저가_특정_게시글을_조회하면_게시글_상세정보가_반환되어야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .get("/IT/{boardType}/{postId}", BoardType.REVIEW, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("postId", equalTo(testPost.getId().intValue()))
                    .body("title", equalTo(testPost.getTitle()))
                    .body("content", equalTo(testPost.getContent()))
                    .body("nickname", equalTo(testUser.getNickname()))
                    .body("boardType", equalTo("REVIEW"));
        }
    }

    @Nested
    class 게시글_작성_테스트 {

        @Test
        void 유저가_게시글을_작성하면_새로운_게시글이_생성되어야_한다() {
            // given
            ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO(
                    "새 게시글 제목", "태그", "새 게시글 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/IT/{boardType}", BoardType.REVIEW)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("title", equalTo("새 게시글 제목"))
                    .body("content", equalTo("새 게시글 내용"));
        }

        @Test
        void 로그인하지_않은_유저는_게시글을_작성할_수_없다() {
            // given
            ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO(
                    "새 게시글 제목", "태그", "새 게시글 내용");

            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/IT/{boardType}", BoardType.REVIEW)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class 게시글_수정_테스트 {

        @Test
        void 유저가_본인_게시글을_수정하면_게시글_내용이_변경되어야_한다() {
            // given
            ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO(
                    "수정된 게시글 제목", "수정된 태그", "수정된 게시글 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/IT/{boardType}/{postId}", BoardType.REVIEW, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 유저가_다른_사용자의_게시글을_수정하려_하면_실패해야_한다() {
            // given
            ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO(
                    "수정된 게시글 제목", "수정된 태그", "수정된 게시글 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + anotherUserToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/IT/{boardType}/{postId}", BoardType.REVIEW, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 로그인하지_않은_유저는_게시글을_수정할_수_없다() {
            // given
            ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO(
                    "수정된 게시글 제목", "수정된 태그", "수정된 게시글 내용");

            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/IT/{boardType}/{postId}", BoardType.REVIEW, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class 게시글_삭제_테스트 {

        @Test
        void 유저가_본인_게시글을_삭제하면_게시글이_제거되어야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .delete("/IT/{boardType}/{postId}", BoardType.REVIEW, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 유저가_다른_사용자의_게시글을_삭제하려_하면_실패해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + anotherUserToken)
                    .when()
                    .delete("/IT/{boardType}/{postId}", BoardType.REVIEW, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 로그인하지_않은_유저는_게시글을_삭제할_수_없다() {
            // when & then
            given()
                    .when()
                    .delete("/IT/{boardType}/{postId}", BoardType.REVIEW, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class 예외_상황_테스트 {

        @Test
        void 유저가_잘못된_보드타입으로_요청하면_400에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .get("/IT/{boardType}", "INVALID_BOARD_TYPE")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 유저가_잘못된_정렬타입으로_요청하면_400에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .param("sortType", "INVALID_SORT_TYPE")
                    .when()
                    .get("/IT/{boardType}", BoardType.REVIEW)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 유저가_음수_페이지로_요청하면_400에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .param("page", "-1")
                    .when()
                    .get("/IT/{boardType}", BoardType.REVIEW)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 존재하지_않는_게시글을_조회하면_404에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .get("/IT/{boardType}/{postId}", BoardType.REVIEW, 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 존재하지_않는_게시글을_수정하려_하면_404에러가_발생해야_한다() {
            // given
            ITRequestDTO requestDTO = ITTestFixtures.createRequestDTO(
                    "수정된 제목", "태그", "수정된 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/IT/{boardType}/{postId}", BoardType.REVIEW, 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 존재하지_않는_게시글을_삭제하려_하면_404에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .delete("/IT/{boardType}/{postId}", BoardType.REVIEW, 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    private void setupTestData() {
        testUser = ITTestFixtures.createStudentUser();
        anotherUser = ITTestFixtures.createGuestUser();

        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);

        testPost = ITTestFixtures.createPost(testUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.REVIEW);
        testPost = postRepository.save(testPost);

        userToken = jwtProvider.generateAccessToken(testUser.getEmail());
        anotherUserToken = jwtProvider.generateAccessToken(anotherUser.getEmail());
    }

    private void cleanUpData() {
        bookmarkRepository.deleteAll();
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}