package kr.co.amateurs.server.controller.community;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.fixture.community.CommunityTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CommunityControllerTest extends AbstractControllerTest {

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
                    .param("size", "8")
                    .param("field", "LATEST")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.totalPages", greaterThanOrEqualTo(0))
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(8));
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
                    .param("size", "8")
                    .param("field", "LATEST")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", lessThanOrEqualTo(8));
        }

        @Test
        void 유저가_특정_게시글을_조회하면_게시글_상세정보가_반환되어야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .get("/community/{boardType}/{postId}", BoardType.FREE, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("postId", equalTo(testPost.getId().intValue()))
                    .body("title", equalTo(testPost.getTitle()))
                    .body("content", equalTo(testPost.getContent()))
                    .body("nickname", equalTo(testUser.getNickname()))
                    .body("boardType", equalTo("FREE"));
        }
    }

    @Nested
    class 게시글_작성_테스트 {

        @Test
        void 유저가_게시글을_작성하면_새로운_게시글이_생성되어야_한다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "새 게시글 제목", "태그", "새 게시글 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("title", equalTo("새 게시글 제목"))
                    .body("content", equalTo("새 게시글 내용"));
        }

        @Test
        void 로그인하지_않은_유저는_게시글을_작성할_수_없다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "새 게시글 제목", "태그", "새 게시글 내용");

            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class 게시글_수정_테스트 {

        @Test
        void 유저가_본인_게시글을_수정하면_게시글_내용이_변경되어야_한다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "수정된 게시글 제목", "수정된 태그", "수정된 게시글 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{postId}", BoardType.FREE, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 유저가_다른_사용자의_게시글을_수정하려_하면_실패해야_한다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "수정된 게시글 제목", "수정된 태그", "수정된 게시글 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + anotherUserToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{postId}", BoardType.FREE, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 로그인하지_않은_유저는_게시글을_수정할_수_없다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "수정된 게시글 제목", "수정된 태그", "수정된 게시글 내용");

            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{postId}", BoardType.FREE, testPost.getId())
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
                    .delete("/community/{boardType}/{postId}", BoardType.FREE, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 유저가_다른_사용자의_게시글을_삭제하려_하면_실패해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + anotherUserToken)
                    .when()
                    .delete("/community/{boardType}/{postId}", BoardType.FREE, testPost.getId())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 로그인하지_않은_유저는_게시글을_삭제할_수_없다() {
            // when & then
            given()
                    .when()
                    .delete("/community/{boardType}/{postId}", BoardType.FREE, testPost.getId())
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
                    .get("/community/{boardType}", "INVALID_BOARD_TYPE")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 유저가_잘못된_정렬필드로_요청하면_400에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .param("field", "INVALID_FIELD")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 유저가_잘못된_정렬방향으로_요청하면_400에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .param("sortDirection", "INVALID_DIRECTION")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
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
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 존재하지_않는_게시글을_조회하면_404에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .get("/community/{boardType}/{postId}", BoardType.FREE, 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 존재하지_않는_게시글을_수정하려_하면_404에러가_발생해야_한다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "수정된 제목", "태그", "수정된 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{postId}", BoardType.FREE, 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 존재하지_않는_게시글을_삭제하려_하면_404에러가_발생해야_한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + userToken)
                    .when()
                    .delete("/community/{boardType}/{postId}", BoardType.FREE, 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    private void setupTestData() {
        testUser = CommunityTestFixtures.createStudentUser();
        anotherUser = CommunityTestFixtures.createGuestUser();

        testUser = userRepository.save(testUser);
        anotherUser = userRepository.save(anotherUser);

        testPost = CommunityTestFixtures.createPost(testUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.FREE);
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