package kr.co.amateurs.server.controller.follow;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.follow.FollowPostResponseDTO;
import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.follow.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class FollowControllerTest extends AbstractControllerTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private FollowService followService;

    private final String BEARER = "Bearer test-jwt-token";

    @BeforeEach
    void setUp() {
        RestAssured.port     = port;
        RestAssured.basePath = "/api/v1";
    }

    @Nested @DisplayName("팔로잉 목록 조회")
    class GetFollowingListTest {
        @Test @DisplayName("성공")
        void success() {
            var mock = List.of(
                    new FollowResponseDTO(1L, "targetUser1", "http://img1.png"),
                    new FollowResponseDTO(2L, "targetUser2", "http://img2.png")
            );
            when(followService.getFollowingList()).thenReturn(mock);

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/users/following")
                    .then()
                    .statusCode(200)
                    .body("", hasSize(2))
                    .body("[0].userId",   equalTo(1))
                    .body("[0].nickname", equalTo("targetUser1"))
                    .body("[0].profileImg", equalTo("http://img1.png"))
                    .body("[1].userId",   equalTo(2))
                    .body("[1].nickname", equalTo("targetUser2"));
        }

        @Test @DisplayName("빈 배열 반환")
        void empty() {
            when(followService.getFollowingList()).thenReturn(List.of());

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/users/following")
                    .then()
                    .statusCode(200)
                    .body("", hasSize(0));
        }
    }

    @Nested @DisplayName("팔로워 목록 조회")
    class GetFollowerListTest {
        @Test @DisplayName("성공")
        void success() {
            var mock = List.of(
                    new FollowResponseDTO(5L, "follower1", "http://imgf1.png"),
                    new FollowResponseDTO(6L, "follower2", "http://imgf2.png")
            );
            when(followService.getFollowerList()).thenReturn(mock);

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/users/follower")
                    .then()
                    .statusCode(200)
                    .body("", hasSize(2))
                    .body("[0].userId",   equalTo(5))
                    .body("[0].nickname", equalTo("follower1"))
                    .body("[1].userId",   equalTo(6))
                    .body("[1].nickname", equalTo("follower2"));
        }

        @Test @DisplayName("빈 배열 반환")
        void empty() {
            when(followService.getFollowerList()).thenReturn(List.of());

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/users/follower")
                    .then()
                    .statusCode(200)
                    .body("", hasSize(0));
        }
    }

    @Nested @DisplayName("팔로우 게시글 조회")
    class GetFollowPostListTest {
        @Test @DisplayName("성공")
        void success() {
            var now = LocalDateTime.now();
            var mock = List.of(
                    new FollowPostResponseDTO(
                            1L, BoardType.FREE,   "자유 게시글",      "자유 게시글 내용",
                            5, 10, "", now, now
                    ),
                    new FollowPostResponseDTO(
                            2L, BoardType.REVIEW, "리뷰 게시글",      "리뷰 게시글 내용",
                            8, 20, "", now, now
                    )
            );
            when(followService.getFollowPostList()).thenReturn(mock);

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/users/followPost")
                    .then()
                    .statusCode(200)
                    .body("", hasSize(2))
                    .body("[0].postId",   equalTo(1))
                    .body("[0].boardType", equalTo("FREE"))
                    .body("[0].title",    equalTo("자유 게시글"))
                    .body("[0].content",  equalTo("자유 게시글 내용"))
                    .body("[0].likeCount", equalTo(5))
                    .body("[0].viewCount", equalTo(10))
                    .body("[1].postId",   equalTo(2))
                    .body("[1].boardType", equalTo("REVIEW"));
        }

        @Test @DisplayName("빈 배열 반환")
        void empty() {
            when(followService.getFollowPostList()).thenReturn(List.of());

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/users/followPost")
                    .then()
                    .statusCode(200)
                    .body("", hasSize(0));
        }
    }

    @Nested @DisplayName("팔로우 추가/제거")
    class AddRemoveFollowTest {
        @Test @DisplayName("추가 성공")
        void addSuccess() {
            var dto = new FollowResponseDTO(2L, "targetUser1", "http://img1.png");
            when(followService.followUser(2L)).thenReturn(dto);

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/users/2/follow")
                    .then()
                    .statusCode(201)
                    .body("userId",   equalTo(2))
                    .body("nickname", equalTo("targetUser1"));
        }

        @Test @DisplayName("자기 자신 팔로우 → 400")
        void selfFollow() {
            when(followService.followUser(1L))
                    .thenThrow(new CustomException(ErrorCode.SELF_FOLLOW));

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/users/1/follow")
                    .then()
                    .statusCode(400)
                    .body("message", equalTo(ErrorCode.SELF_FOLLOW.getMessage()));
        }

        @Test @DisplayName("언팔로우 성공")
        void removeSuccess() {
            doNothing().when(followService).unfollowUser(2L);

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/users/2/follow")
                    .then()
                    .statusCode(204);
        }

        @Test @DisplayName("없는 사용자 언팔로우 → 404")
        void removeNotFound() {
            doNothing().when(followService).unfollowUser(2L);
            doThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
                    .when(followService).unfollowUser(999L);

            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/users/999/follow")
                    .then()
                    .statusCode(404)
                    .body("message", equalTo(ErrorCode.USER_NOT_FOUND.getMessage()));
        }
    }

    @Nested @DisplayName("인증·메서드 오류 처리")
    class AuthMethodTest {
        @Test @DisplayName("토큰 없으면 401")
        void noToken() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/users/following")
                    .then()
                    .statusCode(401);
        }

        @Test @DisplayName("잘못된 메서드 → 405")
        void wrongMethod() {
            given()
                    .header("Authorization", BEARER)
                    .contentType(ContentType.JSON)
                    .when()
                    .patch("/users/following")
                    .then()
                    .statusCode(405);
        }
    }
}
