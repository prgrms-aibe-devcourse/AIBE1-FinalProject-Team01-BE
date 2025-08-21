package kr.co.amateurs.server.controller.alarm;

import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.alarm.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Sql(scripts = "/reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class SseIntegrationTest extends AbstractControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SseService sseService;

    private User testUser;
    private User anotherUser;
    private String accessToken;
    private String anotherAccessToken;

    @BeforeEach
    void setUp() {
        clearAllConnections();
        testUser = userRepository.save(
                UserTestFixture.defaultUser()
                        .email("test@email.com")
                        .nickname("test nickname")
                        .build()
        );

        anotherUser = userRepository.save(
                UserTestFixture.defaultUser()
                        .email("test2@email.com")
                        .nickname("test2 nickname")
                        .build()
        );
        accessToken = jwtProvider.generateAccessToken(testUser.getEmail());
        anotherAccessToken = jwtProvider.generateAccessToken(anotherUser.getEmail());
    }

    @Test
    void 인증된_사용자는_SSE에_연결할_수_있다() {
        // when
        given().header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                .when()
                .get("/sse")
                .then()
                .statusCode(HttpStatus.OK.value());

        assertThat(isUserConnected(testUser.getId())).isTrue();
    }

    @Test
    void 다른_사용자들이_동시에_SSE_연결할_수_있다() {
        // given & when
        given().header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                .when()
                .get("/sse")
                .then()
                .statusCode(HttpStatus.OK.value());

        given().header("Authorization", "Bearer " + anotherAccessToken)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                .when()
                .get("/sse")
                .then()
                .statusCode(HttpStatus.OK.value());

        // then
        assertThat(isUserConnected(testUser.getId())).isTrue();
        assertThat(isUserConnected(anotherUser.getId())).isTrue();
        assertThat(getConnections()).hasSize(2);
    }

    @Test
    void 같은_사용자가_중복_연결_시_기존_연결이_교체된다() {
        // given & when
        given().header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                .when()
                .get("/sse")
                .then()
                .statusCode(HttpStatus.OK.value());

        given().header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                .when()
                .get("/sse")
                .then()
                .statusCode(HttpStatus.OK.value());

        // then
        assertThat(isUserConnected(testUser.getId())).isTrue();
        assertThat(getConnections()).hasSize(1);
    }

    @Test
    void 연결된_사용자는_연결취소시_204를_반환한다() {
        // given
        given().header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                .when()
                .get("/sse")
                .then()
                .statusCode(HttpStatus.OK.value());

        // when
        given().header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                .when()
                .delete("/sse")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(isUserConnected(testUser.getId())).isFalse();
        assertThat(getConnections()).isEmpty();
    }

    // 헬퍼 메서드들
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long, SseEmitter> getConnections() {
        Object field = ReflectionTestUtils.getField(sseService, "connections");
        if (field instanceof ConcurrentHashMap<?, ?>) {
            return (ConcurrentHashMap<Long, SseEmitter>) field;
        }
        throw new RuntimeException();
    }

    private void clearAllConnections() {
        ConcurrentHashMap<Long, SseEmitter> connections = getConnections();
        connections.clear();
    }

    private boolean isUserConnected(Long userId) {
        ConcurrentHashMap<Long, SseEmitter> connections = getConnections();
        return connections.containsKey(userId);
    }
}
