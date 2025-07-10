package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.alarm.AlarmTestFixture;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SseServiceUnitTest {

    @InjectMocks
    private SseService sseService;

    @Mock
    private UserService userService;

    @Mock
    private SseEmitter mockEmitter;

    private User testUser;
    private Alarm testAlarm;
    private ConcurrentHashMap<Long, SseEmitter> connections;

    @BeforeEach
    void setUp() {
        testUser = UserTestFixture.createUser();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testAlarm = AlarmTestFixture.defaultAlarm().build();

        when(userService.getCurrentLoginUser()).thenReturn(testUser);

        connections = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(sseService, "connections", connections);
    }

    @Test
    void 연결된_사용자에게_알람을_성공적으로_전송한다() throws IOException {
        // given
        connections.put(testUser.getId(), mockEmitter);

        // when
        assertDoesNotThrow(() -> sseService.sendAlarmToUser(testUser.getId(), testAlarm));

        // then
        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void 연결되지_않은_사용자에게_알람_전송_시_예외가_발생한다() {
        // given - 연결하지 않음 (connections가 비어있음)

        // when & then
        assertThatThrownBy(() -> sseService.sendAlarmToUser(testUser.getId(), testAlarm))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 알람_전송_중_IOException_발생_시_연결을_제거한다() throws IOException {
        // given
        connections.put(testUser.getId(), mockEmitter);
        doThrow(new IOException("Connection lost")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        // when
        assertDoesNotThrow(() -> sseService.sendAlarmToUser(testUser.getId(), testAlarm));

        // then
        assertThat(connections).doesNotContainKey(testUser.getId());
        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void 여러_사용자에게_동시에_알람을_전송할_수_있다() throws IOException {
        // given
        User anotherUser = UserTestFixture.createUserWithEmail("another@test.com");
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        SseEmitter anotherMockEmitter = mock(SseEmitter.class);

        connections.put(testUser.getId(), mockEmitter);
        connections.put(anotherUser.getId(), anotherMockEmitter);

        // when
        assertDoesNotThrow(() -> {
            sseService.sendAlarmToUser(testUser.getId(), testAlarm);
            sseService.sendAlarmToUser(anotherUser.getId(), testAlarm);
        });

        // then
        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(anotherMockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void 연결되지_않은_상태에서_해제_시도_시_예외가_발생하지_않는다() {
        // given - 연결하지 않음

        // when & then
        assertDoesNotThrow(() -> sseService.disconnect());
    }

    @Test
    void 하트비트를_연결된_모든_사용자에게_전송한다() throws IOException {
        // given
        User anotherUser = UserTestFixture.createUserWithEmail("another@test.com");
        ReflectionTestUtils.setField(anotherUser, "id", 2L);
        SseEmitter anotherMockEmitter = mock(SseEmitter.class);

        when(userService.getCurrentLoginUser()).thenReturn(testUser);

        connections.put(testUser.getId(), mockEmitter);
        connections.put(anotherUser.getId(), anotherMockEmitter);

        // when
        sseService.sendHeartbeat();

        // then
        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(anotherMockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void 하트비트_전송_중_오류_발생_시_해당_연결을_제거한다() throws IOException {
        // given
        connections.put(testUser.getId(), mockEmitter);
        doThrow(new IOException("Connection lost")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        // when
        sseService.sendHeartbeat();

        // then
        assertThat(connections).doesNotContainKey(testUser.getId());
    }

    @Test
    void 연결이_없을_때_하트비트_전송_시_아무_동작을_하지_않는다() {
        // given - 연결 없음 (connections가 비어있음)

        // when & then
        assertDoesNotThrow(() -> sseService.sendHeartbeat());
        verifyNoInteractions(mockEmitter);
    }
}
