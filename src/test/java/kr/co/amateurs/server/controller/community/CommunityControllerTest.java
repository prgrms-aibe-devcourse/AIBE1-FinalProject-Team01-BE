package kr.co.amateurs.server.controller.community;

import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.community.CommunityRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.fixture.community.CommunityTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CommunityControllerTest extends AbstractControllerTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostStatisticsRepository postStatisticsRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CommentRepository commentRepository;

    private User guestUser;
    private User studentUser;
    private User adminUser;
    private User otherUser;

    private Post testPost;
    private CommunityPost testCommunityPost;

    private String guestToken;
    private String studentToken;
    private String adminToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        cleanUpData();
        setupTestData();
    }

    @Nested
    class 익명_유저_테스트 {

        @Test
        void 익명_유저는_게시글_목록을_조회할_수_없다() {
            // when & then
            given()
                    .param("page", "0")
                    .param("size", "8")
                    .param("field", "POST_LATEST")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 익명_유저는_게시글_상세를_조회할_수_없다() {
            // when & then
            given()
                    .when()
                    .get("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 익명_유저는_게시글을_작성할_수_없다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "익명 유저 게시글", "태그", "익명 유저 내용");

            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 익명_유저는_게시글을_수정할_수_없다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "익명 수정 제목", "태그", "익명 수정 내용");

            // when & then
            given()
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 익명_유저는_게시글을_삭제할_수_없다() {
            // when & then
            given()
                    .when()
                    .delete("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class 일반_유저_테스트 {

        @Test
        void 일반_유저는_게시글_목록을_조회할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .param("page", "0")
                    .param("size", "8")
                    .param("field", "POST_LATEST")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.totalPages", greaterThanOrEqualTo(0))
                    .body("pageInfo.pageNumber", equalTo(0))
                    .body("pageInfo.pageSize", equalTo(8));
        }

        @Test
        void 일반_유저는_키워드로_게시글을_검색할_수_있다() {
            // given
            String keyword = "테스트";

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .param("keyword", keyword)
                    .param("page", "0")
                    .param("size", "8")
                    .param("field", "LATEST")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.pageNumber", equalTo(0));
        }

        @Test
        void 일반_유저는_게시글_상세를_조회할_수_있다() {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            CommunityPost freshPost = transactionTemplate.execute(status -> {
                Post post = postRepository.save(
                        CommunityTestFixtures.createPost(studentUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.FREE));

                CommunityPost communityPost = communityRepository.save(
                        CommunityTestFixtures.createCommunityPost(post));

                PostStatistics postStatistics = PostStatistics.from(post);
                postStatisticsRepository.save(postStatistics);

                return communityPost;
            });
            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .get("/community/{boardType}/{communityId}", BoardType.FREE, freshPost.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("communityId", equalTo(freshPost.getId().intValue()))
                    .body("title", equalTo("테스트 게시글 제목"))
                    .body("content", equalTo("테스트 게시글 내용"))
                    .body("boardType", equalTo("FREE"));
        }

        @Test
        void 일반_유저는_커뮤니티_게시글을_작성할_수_있다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "일반 유저 게시글", "태그", "일반 유저 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(201);
        }

        @Test
        void 일반_유저는_본인_게시글을_수정할_수_있다() {
            // given - 일반 유저가 작성한 게시글 생성
            Post guestPost = postRepository.save(
                    CommunityTestFixtures.createPost(guestUser, "일반유저 게시글", "일반유저 내용", BoardType.FREE));
            CommunityPost guestCommunityPost = communityRepository.save(
                    CommunityTestFixtures.createCommunityPost(guestPost));

            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "일반 유저 수정 제목", "수정태그", "일반 유저 수정 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{communityId}", BoardType.FREE, guestCommunityPost.getId())
                    .then()
                    .statusCode(204);
        }

        @Test
        void 일반_유저는_본인_게시글을_삭제할_수_있다() {
            // given - 일반 유저가 작성한 게시글 생성
            Post guestPost = postRepository.save(
                    CommunityTestFixtures.createPost(guestUser, "일반유저 게시글", "일반유저 내용", BoardType.FREE));
            CommunityPost guestCommunityPost = communityRepository.save(
                    CommunityTestFixtures.createCommunityPost(guestPost));

            // when & then
            given()
                    .header("Authorization", "Bearer " + guestToken)
                    .when()
                    .delete("/community/{boardType}/{communityId}", BoardType.FREE, guestCommunityPost.getId())
                    .then()
                    .statusCode(204);
        }
    }

    @Nested
    class 학생_유저_테스트 {

        @Test
        void 학생_유저는_게시글_목록을_조회할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("page", "0")
                    .param("size", "8")
                    .param("field", "POST_LATEST")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.totalPages", greaterThanOrEqualTo(0));
        }

        @Test
        void 학생_유저는_인기순으로_정렬하여_조회할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("page", "0")
                    .param("size", "8")
                    .param("field", "POST_POPULAR")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.pageNumber", equalTo(0));
        }

        @Test
        void 학생_유저는_게시글_상세를_조회할_수_있다() {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            CommunityPost freshPost = transactionTemplate.execute(status -> {
                Post post = postRepository.save(
                        CommunityTestFixtures.createPost(studentUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.FREE));

                CommunityPost communityPost = communityRepository.save(
                        CommunityTestFixtures.createCommunityPost(post));

                PostStatistics postStatistics = PostStatistics.from(post);
                postStatisticsRepository.save(postStatistics);

                return communityPost;
            });
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/community/{boardType}/{communityId}", BoardType.FREE, freshPost.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("communityId", equalTo(freshPost.getId().intValue()));
        }

        @Test
        void 학생_유저는_게시글을_작성할_수_있다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "학생 유저 게시글", "학습태그", "학생 유저 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("title", equalTo("학생 유저 게시글"))
                    .body("content", equalTo("학생 유저 내용"));
        }

        @Test
        void 학생_유저는_본인_게시글을_수정할_수_있다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "학생이 수정한 제목", "수정태그", "학생이 수정한 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 학생_유저는_본인_게시글을_삭제할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .delete("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }
    }

    @Nested
    class 관리자_유저_테스트 {

        @Test
        void 관리자는_게시글_목록을_조회할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .param("page", "0")
                    .param("size", "8")
                    .param("field", "POST_LATEST")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("pageInfo.totalPages", greaterThanOrEqualTo(0));
        }

        @Test
        void 관리자는_게시글_상세를_조회할_수_있다() {

            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            CommunityPost freshPost = transactionTemplate.execute(status -> {
                Post post = postRepository.save(
                        CommunityTestFixtures.createPost(studentUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.FREE));

                CommunityPost communityPost = communityRepository.save(
                        CommunityTestFixtures.createCommunityPost(post));

                PostStatistics postStatistics = PostStatistics.from(post);
                postStatisticsRepository.save(postStatistics);

                return communityPost;
            });

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/community/{boardType}/{communityId}", BoardType.FREE, freshPost.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("communityId", equalTo(freshPost.getId().intValue()));
        }

        @Test
        void 관리자는_게시글을_작성할_수_있다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "관리자 공지사항", "공지태그", "관리자 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .post("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("title", equalTo("관리자 공지사항"));
        }

        @Test
        void 관리자는_다른_사용자의_게시글을_수정할_수_있다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "관리자가 수정한 제목", "관리태그", "관리자가 수정한 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 관리자는_다른_사용자의_게시글을_삭제할_수_있다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .delete("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }
    }

    @Nested
    class 다른_유저_권한_테스트 {

        @Test
        void 다른_유저는_다른_사람의_게시글을_수정할_수_없다() {
            // given
            CommunityRequestDTO requestDTO = CommunityTestFixtures.createRequestDTO(
                    "다른 유저 수정 시도", "태그", "다른 유저 수정 내용");

            // when & then
            given()
                    .header("Authorization", "Bearer " + otherUserToken)
                    .contentType(ContentType.JSON)
                    .body(requestDTO)
                    .when()
                    .put("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @Test
        void 다른_유저는_다른_사람의_게시글을_삭제할_수_없다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + otherUserToken)
                    .when()
                    .delete("/community/{boardType}/{communityId}", BoardType.FREE, testCommunityPost.getId())
                    .then()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class 예외_상황_테스트 {

        @Test
        void 잘못된_보드타입으로_요청하면_400에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/community/{boardType}", "INVALID_BOARD_TYPE")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 존재하지_않는_communityId로_조회하면_404에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .when()
                    .get("/community/{boardType}/{communityId}", BoardType.FREE, 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 잘못된_정렬_필드로_요청하면_400에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("field", "INVALID_FIELD")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 음수_페이지로_요청하면_400에러가_발생한다() {
            // when & then
            given()
                    .header("Authorization", "Bearer " + studentToken)
                    .param("page", "-1")
                    .when()
                    .get("/community/{boardType}", BoardType.FREE)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    private void setupTestData() {
        guestUser = userRepository.save(CommunityTestFixtures.createGuestUser());
        studentUser = userRepository.save(CommunityTestFixtures.createStudentUser());
        adminUser = userRepository.save(CommunityTestFixtures.createAdminUser());
        otherUser = userRepository.save(CommunityTestFixtures.createCustomUser(
                "other@test.com", "other", "다른사용자", Role.STUDENT));

        testPost = postRepository.save(
                CommunityTestFixtures.createPost(studentUser, "테스트 게시글 제목", "테스트 게시글 내용", BoardType.FREE));
        testCommunityPost = communityRepository.save(
                CommunityTestFixtures.createCommunityPost(testPost));

        guestToken = jwtProvider.generateAccessToken(guestUser.getEmail());
        studentToken = jwtProvider.generateAccessToken(studentUser.getEmail());
        adminToken = jwtProvider.generateAccessToken(adminUser.getEmail());
        otherUserToken = jwtProvider.generateAccessToken(otherUser.getEmail());
    }

    private void cleanUpData() {
        commentRepository.deleteAll();
        postStatisticsRepository.deleteAll();
        communityRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
}