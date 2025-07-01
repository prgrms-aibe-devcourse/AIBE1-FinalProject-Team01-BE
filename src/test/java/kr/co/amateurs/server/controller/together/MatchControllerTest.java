package kr.co.amateurs.server.controller.together;

import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.together.MatchTestFixture;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Transactional
class MatchControllerTest extends AbstractControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MatchRepository matchRepository;

    private User testUser;
    private String userToken;

//    @BeforeEach
//    void setUp() {
//        testUser = userRepository.save(MatchTestFixture.createDefaultUser());
//        userToken = createTokenForUser(testUser);
//    }

//    @Nested
//    @DisplayName("커피챗/멘토링 글 목록 조회")
//    class GetMatchPostList {
//
//        @BeforeEach
//        void setUpPosts() {
//            // 테스트 데이터 생성
//            User user1 = userRepository.save(MatchTestFixture.createDefaultUser());
//            User user2 = userRepository.save(MatchTestFixture.createSecondUser());
//
//            Post post1 = postRepository.save(MatchTestFixture.createPostWithUser(user1));
//            Post post2 = postRepository.save(MatchTestFixture.createPostWithUser(user2));
//
//            matchRepository.save(MatchTestFixture.createMatchingPost(
//                    post1, MatchingType.COFFEE_CHAT, MatchingStatus.OPEN, "Frontend"
//            ));
//            matchRepository.save(MatchTestFixture.createMatchingPost(
//                    post2, MatchingType.MENTORING, MatchingStatus.OPEN, "Backend"
//            ));
//        }
//
//        @Test
//        @DisplayName("인증된 사용자는 게시글 목록을 조회할 수 있다")
//        void 인증된_사용자는_게시글_목록을_조회할_수_있다() {
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .param("page", 0)
//                    .param("size", 10)
//                    .when()
//                    .get("/api/v1/matches")
//                    .then()
//                    .statusCode(200)
//                    .time(lessThan(2000L))
//                    .body("content", hasSize(greaterThanOrEqualTo(0)))
//                    .body("totalElements", greaterThanOrEqualTo(0))
//                    .body("totalPages", greaterThanOrEqualTo(0))
//                    .body("currentPage", equalTo(0))
//                    .body("pageSize", equalTo(10));
//        }
//
//        @Test
//        @DisplayName("키워드로 검색하여 게시글을 조회할 수 있다")
//        void 키워드로_검색하여_게시글을_조회할_수_있다() {
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .param("keyword", "멘토링")
//                    .param("page", 0)
//                    .param("size", 10)
//                    .when()
//                    .get("/api/v1/matches")
//                    .then()
//                    .statusCode(200)
//                    .time(lessThan(2000L))
//                    .body("content", hasSize(greaterThanOrEqualTo(0)));
//        }
//
//        @Test
//        @DisplayName("인증되지 않은 사용자는 401 에러를 받는다")
//        void 인증되지_않은_사용자는_401_에러를_받는다() {
//            given()
//                    .param("page", 0)
//                    .param("size", 10)
//                    .when()
//                    .get("/api/v1/matches")
//                    .then()
//                    .statusCode(401);
//        }
//    }
//
//    @Nested
//    @DisplayName("커피챗/멘토링 글 상세 조회")
//    class GetMatchPost {
//
//        private Long postId;
//
//        @BeforeEach
//        void setUpPost() {
//            Post post = postRepository.save(MatchTestFixture.createPostWithUser(testUser));
//            MatchingPost matchingPost = matchRepository.save(
//                    MatchTestFixture.createMatchingPost(post, MatchingType.COFFEE_CHAT, MatchingStatus.OPEN, "Frontend")
//            );
//            postId = post.getId();
//        }
//
//        @Test
//        @DisplayName("존재하는 게시글을 조회할 수 있다")
//        void 존재하는_게시글을_조회할_수_있다() {
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .when()
//                    .get("/api/v1/matches/{postId}", postId)
//                    .then()
//                    .statusCode(200)
//                    .time(lessThan(2000L))
//                    .body("postId", equalTo(postId.intValue()))
//                    .body("title", notNullValue())
//                    .body("content", notNullValue())
//                    .body("matchingType", notNullValue())
//                    .body("status", notNullValue())
//                    .body("nickname", notNullValue());
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 게시글 조회 시 404 에러를 받는다")
//        void 존재하지_않는_게시글_조회시_404_에러를_받는다() {
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .when()
//                    .get("/api/v1/matches/{postId}", MatchTestFixture.INVALID_POST_ID)
//                    .then()
//                    .statusCode(404);
//        }
//    }
//
//    @Nested
//    @DisplayName("커피챗/멘토링 글 작성")
//    class CreateMatchPost {
//
//        @Test
//        @DisplayName("유효한 데이터로 게시글을 작성할 수 있다")
//        void 유효한_데이터로_게시글을_작성할_수_있다() {
//            MatchPostRequestDTO requestDTO = MatchTestFixture.createMatchPostRequest();
//
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .contentType(ContentType.JSON)
//                    .body(requestDTO)
//                    .when()
//                    .post("/api/v1/matches")
//                    .then()
//                    .statusCode(201)
//                    .time(lessThan(2000L))
//                    .body("postId", notNullValue())
//                    .body("title", equalTo(requestDTO.title()))
//                    .body("content", equalTo(requestDTO.content()))
//                    .body("matchingType", equalTo(requestDTO.matchingType().name()))
//                    .body("status", equalTo(requestDTO.status().name()))
//                    .body("expertiseArea", equalTo(requestDTO.expertiseArea()));
//        }
//
//        @Test
//        @DisplayName("멘토링 게시글을 작성할 수 있다")
//        void 멘토링_게시글을_작성할_수_있다() {
//            MatchPostRequestDTO requestDTO = MatchTestFixture.createMentoringRequest();
//
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .contentType(ContentType.JSON)
//                    .body(requestDTO)
//                    .when()
//                    .post("/api/v1/matches")
//                    .then()
//                    .statusCode(201)
//                    .time(lessThan(2000L))
//                    .body("matchingType", equalTo("MENTORING"))
//                    .body("expertiseArea", equalTo("Backend Development"));
//        }
//
//        @Test
//        @DisplayName("필수 필드가 누락된 경우 400 에러를 받는다")
//        void 필수_필드가_누락된_경우_400_에러를_받는다() {
//            MatchPostRequestDTO invalidDTO = MatchTestFixture.createInvalidRequest();
//
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .contentType(ContentType.JSON)
//                    .body(invalidDTO)
//                    .when()
//                    .post("/api/v1/matches")
//                    .then()
//                    .statusCode(400);
//        }
//
//        @Test
//        @DisplayName("인증되지 않은 사용자는 401 에러를 받는다")
//        void 인증되지_않은_사용자는_401_에러를_받는다() {
//            MatchPostRequestDTO requestDTO = MatchTestFixture.createMatchPostRequest();
//
//            given()
//                    .contentType(ContentType.JSON)
//                    .body(requestDTO)
//                    .when()
//                    .post("/api/v1/matches")
//                    .then()
//                    .statusCode(401);
//        }
//    }
//
//    @Nested
//    @DisplayName("커피챗/멘토링 글 수정")
//    class UpdateMatchPost {
//
//        private Long postId;
//
//        @BeforeEach
//        void setUpPost() {
//            Post post = postRepository.save(MatchTestFixture.createPostWithUser(testUser));
//            matchRepository.save(
//                    MatchTestFixture.createMatchingPost(post, MatchingType.COFFEE_CHAT, MatchingStatus.OPEN, "Frontend")
//            );
//            postId = post.getId();
//        }
//
//        @Test
//        @DisplayName("작성자는 자신의 게시글을 수정할 수 있다")
//        void 작성자는_자신의_게시글을_수정할_수_있다() {
//            MatchPostRequestDTO updateDTO = MatchTestFixture.createMentoringRequest();
//
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .contentType(ContentType.JSON)
//                    .body(updateDTO)
//                    .when()
//                    .put("/api/v1/matches/{postId}", postId)
//                    .then()
//                    .statusCode(204)
//                    .time(lessThan(2000L));
//        }
//
//        @Test
//        @DisplayName("다른 사용자의 게시글 수정 시 403 에러를 받는다")
//        void 다른_사용자의_게시글_수정시_403_에러를_받는다() {
//            User otherUser = userRepository.save(MatchTestFixture.createSecondUser());
//            String otherUserToken = createTokenForUser(otherUser);
//
//            MatchPostRequestDTO updateDTO = MatchTestFixture.createMentoringRequest();
//
//            given()
//                    .header("Authorization", "Bearer " + otherUserToken)
//                    .contentType(ContentType.JSON)
//                    .body(updateDTO)
//                    .when()
//                    .put("/api/v1/matches/{postId}", postId)
//                    .then()
//                    .statusCode(403);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 게시글 수정 시 404 에러를 받는다")
//        void 존재하지_않는_게시글_수정시_404_에러를_받는다() {
//            MatchPostRequestDTO updateDTO = MatchTestFixture.createMatchPostRequest();
//
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .contentType(ContentType.JSON)
//                    .body(updateDTO)
//                    .when()
//                    .put("/api/v1/matches/{postId}", MatchTestFixture.INVALID_POST_ID)
//                    .then()
//                    .statusCode(404);
//        }
//    }
//
//    @Nested
//    @DisplayName("커피챗/멘토링 글 삭제")
//    class DeleteMatchPost {
//
//        private Long postId;
//
//        @BeforeEach
//        void setUpPost() {
//            Post post = postRepository.save(MatchTestFixture.createPostWithUser(testUser));
//            matchRepository.save(
//                    MatchTestFixture.createMatchingPost(post, MatchingType.COFFEE_CHAT, MatchingStatus.OPEN, "Frontend")
//            );
//            postId = post.getId();
//        }
//
//        @Test
//        @DisplayName("작성자는 자신의 게시글을 삭제할 수 있다")
//        void 작성자는_자신의_게시글을_삭제할_수_있다() {
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .when()
//                    .delete("/api/v1/matches/{postId}", postId)
//                    .then()
//                    .statusCode(204)
//                    .time(lessThan(2000L));
//        }
//
//        @Test
//        @DisplayName("다른 사용자의 게시글 삭제 시 403 에러를 받는다")
//        void 다른_사용자의_게시글_삭제시_403_에러를_받는다() {
//            User otherUser = userRepository.save(MatchTestFixture.createSecondUser());
//            String otherUserToken = createTokenForUser(otherUser);
//
//            given()
//                    .header("Authorization", "Bearer " + otherUserToken)
//                    .when()
//                    .delete("/api/v1/matches/{postId}", postId)
//                    .then()
//                    .statusCode(403);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 게시글 삭제 시 404 에러를 받는다")
//        void 존재하지_않는_게시글_삭제시_404_에러를_받는다() {
//            given()
//                    .header("Authorization", "Bearer " + userToken)
//                    .when()
//                    .delete("/api/v1/matches/{postId}", MatchTestFixture.INVALID_POST_ID)
//                    .then()
//                    .statusCode(404);
//        }
//    }
//
//    @Nested
//    @DisplayName("권한 및 인증 테스트")
//    class AuthorizationTest {
//
//        @Test
//        @DisplayName("ADMIN 권한으로 모든 API에 접근할 수 있다")
//        void ADMIN_권한으로_모든_API에_접근할_수_있다() {
//            User adminUser = MatchTestFixture.createUser("admin@test.com", "admin", "관리자");
//            adminUser = userRepository.save(adminUser);
//            String adminToken = createTokenForUser(adminUser);
//
//            // 게시글 목록 조회
//            given()
//                    .header("Authorization", "Bearer " + adminToken)
//                    .param("page", 0)
//                    .param("size", 10)
//                    .when()
//                    .get("/api/v1/matches")
//                    .then()
//                    .statusCode(200);
//
//            // 게시글 작성
//            MatchPostRequestDTO requestDTO = MatchTestFixture.createMatchPostRequest();
//            given()
//                    .header("Authorization", "Bearer " + adminToken)
//                    .contentType(ContentType.JSON)
//                    .body(requestDTO)
//                    .when()
//                    .post("/api/v1/matches")
//                    .then()
//                    .statusCode(201);
//        }
//
//        @Test
//        @DisplayName("만료된 토큰으로 접근 시 401 에러를 받는다")
//        void 만료된_토큰으로_접근시_401_에러를_받는다() {
//            String expiredToken = MatchTestFixture.createExpiredToken();
//
//            given()
//                    .header("Authorization", "Bearer " + expiredToken)
//                    .param("page", 0)
//                    .param("size", 10)
//                    .when()
//                    .get("/api/v1/matches")
//                    .then()
//                    .statusCode(401);
//        }
//    }
//
//    // 테스트용 JWT 토큰 생성 헬퍼 메서드
//    private String createTokenForUser(User user) {
//        // 실제 JWT 토큰 생성 로직 또는 테스트용 토큰 반환
//        return "Bearer-" + user.getId();
//    }
}