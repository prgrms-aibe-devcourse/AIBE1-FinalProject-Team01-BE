package kr.co.amateurs.server.controller.directmessage;

import jakarta.annotation.PostConstruct;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.directmessage.DirectMessageFixture;
import kr.co.amateurs.server.fixture.directmessage.DirectMessageRoomFixture;
import kr.co.amateurs.server.fixture.project.UserFixture;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Sql(scripts = "/reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DirectMessageIntegrationTest extends AbstractControllerTest {

    @Autowired
    private List<MongoRepository<?, ?>> mongoRepositories;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DirectMessageRoomFixture roomFixture;

    @Autowired
    private DirectMessageFixture messageFixture;

    private User user1;
    private User user2;
    private User user3;
    private String roomId1;
    private String token;

    @PostConstruct
    void reset() {
        mongoRepositories.forEach(MongoRepository::deleteAll);
    }

    @BeforeEach
    void setUp() {
        setUpData();
        token = jwtProvider.generateAccessToken(user1.getEmail());
    }

    @AfterEach
    void rollback() {
        mongoRepositories.forEach(MongoRepository::deleteAll);
    }

    private void setUpData() {
        createUsers();
        createRooms();
    }

    private void createUsers() {
        user1 = UserFixture.createStudentUser(DevCourseTrack.AI_BACKEND, "1");
        user2 = UserFixture.createStudentUser(DevCourseTrack.FRONTEND, "2");
        user3 = UserFixture.createStudentUser(DevCourseTrack.AI_BACKEND, "3");

        List<User> savedUsers = userRepository.saveAll(List.of(user1, user2, user3));
        user1 = savedUsers.get(0);
        user2 = savedUsers.get(1);
        user3 = savedUsers.get(2);
    }

    private void createRooms() {
        DirectMessageRoom room1 = roomFixture.createAndSaveRoom(
                "room1",
                user1.getId(),
                user1.getNickname(),
                user2.getId(),
                user2.getNickname()
        );

        roomFixture.createAndSaveRoom(
                "room2",
                user1.getId(),
                user1.getNickname(),
                user3.getId(),
                user3.getNickname()
        );

        roomId1 = room1.getId();
    }

    @Nested
    class 채팅방_생성 {

        @Nested
        class 성공 {

            @Test
            void 정상적인_채팅방_생성_요청_시_201_응답과_방_정보가_반환된다() {
                // given

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .post("/dm/{partnerId}", user2.getId())
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.CREATED.value())
                        .body("id", notNullValue())
                        .body("partnerId", equalTo(user2.getId().intValue()))
                        .body("partnerNickname", equalTo(user2.getNickname()));
            }

            @Test
            void 이미_존재하는_채팅방_생성_시_기존_방이_반환된다() {
                // given
                String firstRoomId = given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .post("/dm/{partnerId}", user2.getId())
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .post("/dm/{partnerId}", user2.getId())
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.CREATED.value())
                        .body("id", equalTo(firstRoomId))
                        .body("partnerId", equalTo(user2.getId().intValue()));
            }
        }

        @Nested
        class 실패 {

            @Test
            void 존재하지_않는_사용자와_채팅방_생성_시_404_에러가_발생한다() {
                // given
                Long nonExistentUserId = 999L;

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .post("/dm/{partnerId}", nonExistentUserId)
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NOT_FOUND.value());
            }

            @Test
            void 자기_자신과_채팅방_생성_시_400_에러가_발생한다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .post("/dm/{partnerId}", user1.getId())
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }
        }
    }

    @Nested
    class 채팅방_목록_조회 {

        @Nested
        class 성공 {

            @Test
            void 정상적인_채팅방_목록_조회_시_200과_방_목록이_반환된다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .get("/dm/rooms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("size()", equalTo(2))
                        .body("[0].id", notNullValue())
                        .body("[0].partnerId", notNullValue())
                        .body("[0].partnerNickname", notNullValue());
            }

            @Test
            void 채팅방이_없는_사용자_조회_시_빈_배열이_반환된다() {
                // given
                String token = jwtProvider.generateAccessToken(user3.getEmail());

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .get("/dm/rooms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("size()", equalTo(1));
            }
        }
    }

    @Nested
    class 채팅_기록_조회 {

        @Nested
        class 성공 {

            @Test
            void 정상적인_채팅_기록_조회_시_200_응답과_메시지_목록이_반환된다() {
                // given
                messageFixture.createTestMessages(roomId1);

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .queryParam("roomId", roomId1)
                        .queryParam("userId", user1.getId())
                        .when()
                        .get("/dm/messages")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("messages.size()", greaterThan(0))
                        .body("messages[0].id", notNullValue())
                        .body("messages[0].content", notNullValue())
                        .body("messages[0].senderId", notNullValue())
                        .body("messages[0].senderNickname", notNullValue())
                        .body("pageInfo.pageNumber", equalTo(0))
                        .body("pageInfo.totalElements", greaterThan(0));
            }

            @Test
            void 메시지가_없는_채팅방_조회_시_빈_메시지_목록이_반환된다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .queryParam("roomId", roomId1)
                        .queryParam("userId", user1.getId())
                        .when()
                        .get("/dm/messages")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("messages.size()", equalTo(0))
                        .body("pageInfo.totalElements", equalTo(0));
            }

            @Test
            void 페이지네이션_파라미터가_정상적으로_작동한다() {
                // given
                messageFixture.createMultipleMessages(roomId1, 15);

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .queryParam("roomId", roomId1)
                        .queryParam("userId", user1.getId())
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .when()
                        .get("/dm/messages")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("messages.size()", equalTo(10))
                        .body("pageInfo.pageNumber", equalTo(0))
                        .body("pageInfo.pageSize", equalTo(10))
                        .body("pageInfo.totalElements", equalTo(15));
            }
        }

        @Nested
        class 실패 {

            @Test
            void 필수_파라미터_roomId_누락_시_400_에러가_발생한다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .queryParam("userId", user1.getId())
                        .when()
                        .get("/dm/messages")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            void 필수_파라미터_userId_누락_시_400_에러가_발생한다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .queryParam("roomId", roomId1)
                        .when()
                        .get("/dm/messages")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            void 참여하지_않은_채팅방_조회_시_403_에러가_발생한다() {
                // given
                String token = jwtProvider.generateAccessToken(user3.getEmail());

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .queryParam("roomId", roomId1)
                        .queryParam("userId", user3.getId())
                        .when()
                        .get("/dm/messages")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            void 존재하지_않는_채팅방_조회_시_404_에러가_발생한다() {
                // given
                String nonExistentRoomId = "nonexistent-room";

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .queryParam("roomId", nonExistentRoomId)
                        .queryParam("userId", user1.getId())
                        .when()
                        .get("/dm/messages")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NOT_FOUND.value());
            }
        }
    }

    @Nested
    class 채팅방_나가기 {

        @Nested
        class 성공 {

            @Test
            void 정상적인_방_나가기_요청_시_204_응답이_반환된다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .delete("/dm/" + roomId1)
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NO_CONTENT.value());
            }
        }

        @Nested
        class 실패 {

            @Test
            void 참여하지_않은_채팅방_나가기_시_400_에러가_발생한다() {
                // given
                String token = jwtProvider.generateAccessToken(user3.getEmail());

                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .delete("/dm/" + roomId1)
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            void 존재하지_않는_채팅방_나가기_시_404_에러가_발생한다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .when()
                        .delete("/dm/emptyRoom")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NOT_FOUND.value());
            }
        }
    }
}
