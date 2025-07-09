package kr.co.amateurs.server.controller.alarm;

import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageRequest;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.alarm.AlarmTestFixture;
import kr.co.amateurs.server.fixture.directmessage.DirectMessageFixture;
import kr.co.amateurs.server.fixture.directmessage.DirectMessageRoomFixture;
import kr.co.amateurs.server.fixture.project.UserFixture;
import kr.co.amateurs.server.repository.alarm.AlarmRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

class AlarmControllerTest extends AbstractControllerTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DirectMessageService directMessageService;

    @Autowired
    private AlarmTestFixture alarmFixture;

    @Autowired
    private DirectMessageFixture directMessageFixture;

    @Autowired
    private DirectMessageRoomFixture directMessageRoomFixture;

    private User testUser;
    private String accessToken;

    @PostConstruct
    void init() {
        testUser = UserFixture.createStudentUser(DevCourseTrack.AI_BACKEND, "1");
        testUser = userRepository.save(testUser);

        accessToken = jwtProvider.generateAccessToken(testUser.getEmail());
    }

    @BeforeEach
    void setUp() {
        alarmRepository.deleteAll();
    }

    @Nested
    class 알람_목록_조회 {

        @Nested
        class 성공 {

            @Test
            void 인증된_사용자는_본인의_알람_목록을_페이지네이션으로_조회할_수_있다() {
                // given
                alarmFixture.createMultipleAlarms(testUser.getId(), 15);

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("alarms.size()", equalTo(10))
                        .body("pageInfo.pageNumber", equalTo(0))
                        .body("pageInfo.totalElements", equalTo(15))
                        .body("pageInfo.totalPages", equalTo(2))
                        .body("alarms[0].id", notNullValue())
                        .body("alarms[0].type", oneOf("COMMENT", "REPLY", "DIRECT_MESSAGE"))
                        .body("alarms[0].title", notNullValue())
                        .body("alarms[0].content", notNullValue())
                        .body("alarms[0].sentAt", notNullValue());
            }

            @Test
            void 알람이_없는_사용자는_빈_목록을_조회할_수_있다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .param("page", 0)
                        .param("size", 10)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("alarms.size()", equalTo(0))
                        .body("pageInfo.pageNumber", equalTo(0))
                        .body("pageInfo.totalElements", equalTo(0))
                        .body("pageInfo.totalPages", equalTo(0));
            }

            @Test
            void 읽은_알람과_읽지_않은_알람이_모두_조회된다() {
                // given
                int count = 20;
                alarmFixture.createMixedReadStatusAlarms(testUser.getId(), count);

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .param("page", 0)
                        .param("size", count)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("alarms.size()", equalTo(count))
                        .body("alarms.findAll { it.isRead == false }.size()", equalTo(count / 2))
                        .body("alarms.findAll { it.isRead == true }.size()", equalTo(count / 2));
            }

            @Test
            void 다양한_타입의_알람이_모두_조회된다() {
                // given
                alarmFixture.createAlarmsWithDifferentTypes(testUser.getId());

                String[] alarmTypeNames = Arrays.stream(AlarmType.values())
                        .map(Enum::name)
                        .toArray(String[]::new);

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .param("page", 0)
                        .param("size", 10)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("alarms.size()", equalTo(3))
                        .body("alarms.type", hasItems(alarmTypeNames));
            }

            @Test
            void 페이지_크기를_지정하여_조회할_수_있다() {
                // given
                int count = 20;
                int size = 5;
                alarmFixture.createMultipleAlarms(testUser.getId(), count);

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .param("page", 0)
                        .param("size", size)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("alarms.size()", equalTo(size))
                        .body("pageInfo.totalElements", equalTo(count))
                        .body("pageInfo.totalPages", equalTo(count / size));
            }

            @Test
            void 두_번째_페이지를_조회할_수_있다() {
                // given
                alarmFixture.createMultipleAlarms(testUser.getId(), 15);

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .param("page", 1)
                        .param("size", 10)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.OK.value())
                        .body("alarms.size()", equalTo(5))
                        .body("pageInfo.pageNumber", equalTo(1));
            }
        }

        @Nested
        class 실패 {

            @Test
            void 음수_페이지_번호로_요청하면_400_에러가_발생한다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .param("page", -1)
                        .param("size", 10)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            void 페이지_크기가_0_이하면_400_에러가_발생한다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .param("page", 0)
                        .param("size", 0)
                        .when()
                        .get("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.BAD_REQUEST.value());
            }
        }
    }

    @Nested
    class 알람_전체_읽음_처리 {

        @Nested
        class 성공 {

            @Test
            void 읽지_않은_알람이_있는_사용자는_전체_읽음_처리할_수_있다() {
                // given
                alarmFixture.createMixedReadStatusAlarms(testUser.getId(), 20);

                List<Alarm> beforeAlarms = alarmRepository.findByUserId(testUser.getId(), Pageable.unpaged())
                        .getContent();
                long unreadCount = beforeAlarms.stream().filter(i -> !i.isRead()).count();
                assertThat(unreadCount).isEqualTo(10);

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .when()
                        .patch("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NO_CONTENT.value());

                List<Alarm> afterAlarms = alarmRepository.findByUserId(testUser.getId(),
                        Pageable.unpaged()).getContent();
                long unreadCountAfter = afterAlarms.stream().filter(i -> !i.isRead()).count();
                assertThat(unreadCountAfter).isZero();
            }

            @Test
            void 알람이_없는_사용자도_전체_읽음_처리를_요청할_수_있다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .when()
                        .patch("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NO_CONTENT.value());
            }

            @Test
            void 이미_모든_알람이_읽은_상태인_사용자도_전체_읽음_처리를_요청할_수_있다() {
                // given
                alarmFixture.createReadAlarm(testUser.getId());

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .when()
                        .patch("/alarms")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NO_CONTENT.value());
            }
        }
    }

    @Nested
    class 알람_단건_읽음_처리 {

        @Nested
        class 성공 {

            @Test
            void 읽지_않은_알람을_읽음_처리할_수_있다() {
                // given
                Alarm alarm = alarmFixture.createUnReadAlarm(testUser.getId());

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .when()
                        .patch("/alarms/{alarmId}", alarm.getId())
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NO_CONTENT.value());

                // 처리 후 상태 확인
                Alarm readAlarm = alarmRepository.findById(alarm.getId()).orElseThrow();
                assertThat(readAlarm.isRead()).isTrue();
            }

            @Test
            void 이미_읽은_알람을_다시_읽음_처리할_수_있다() {
                // given
                alarmFixture.createReadAlarm(testUser.getId());

                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .when()
                        .patch("/alarms/{alarmId}", "read_" + testUser.getId())
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NO_CONTENT.value());
            }

            @Test
            void 존재하지_않는_알람_ID로_요청해도_성공한다() {
                // when & then
                given()
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .when()
                        .patch("/alarms/{alarmId}", "nonexistent-alarm-id")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.NO_CONTENT.value());
            }
        }
    }

    @Nested
    class 알람생성 {

        @Nested
        class 성공 {

            @Test
            void 알람_트리거를_설정한_함수가_실행되면_알람이_생성된다() {
                // given
                directMessageRoomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
                directMessageFixture.createAndSaveMessage(DirectMessageRoomFixture.ROOM_1, DirectMessageFixture.MESSAGE_CONTENT_1);
                DirectMessageRequest request = new DirectMessageRequest(
                        DirectMessageFixture.MESSAGE_CONTENT_1,
                        testUser.getId(),
                        testUser.getNickname(),
                        MessageType.TEXT
                );

                // when
                directMessageService.saveMessage(DirectMessageRoomFixture.ROOM_1, request);

                // then
                int size = alarmRepository.findAll().size();
                assertThat(size).isGreaterThan(0);
            }
        }
    }
}
