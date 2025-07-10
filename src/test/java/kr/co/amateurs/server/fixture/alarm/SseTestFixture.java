package kr.co.amateurs.server.fixture.alarm;

import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.metadata.DirectMessageMetaData;
import kr.co.amateurs.server.service.alarm.SseService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SseTestFixture {

    private final SseService sseService;

    public SseTestFixture(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * 테스트용 DM 알람 생성 (기존 AlarmTestFixture에 없는 것만)
     */
    public static Alarm createDirectMessageAlarm(Long userId) {
        return AlarmTestFixture.defaultAlarm()
                .id("test_dm_" + userId + "_" + System.currentTimeMillis())
                .userId(userId)
                .type(kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType.DIRECT_MESSAGE)
                .title("새로운 메시지가 도착했습니다.")
                .content("새로운 다이렉트 메시지가 도착했습니다.")
                .metaData(new DirectMessageMetaData("test-room-id","test-message-id"))
                .build();
    }

    /**
     * 현재 활성 연결 수 조회 (리플렉션 사용)
     */
    public int getActiveConnectionCount() {
        try {
            Field connectionsField = SseService.class.getDeclaredField("connections");
            connectionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<Long, SseEmitter> connections = 
                (ConcurrentHashMap<Long, SseEmitter>) connectionsField.get(sseService);
            return connections.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 특정 사용자 연결 확인 (리플렉션 사용)
     */
    public boolean isUserConnected(Long userId) {
        try {
            Field connectionsField = SseService.class.getDeclaredField("connections");
            connectionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<Long, SseEmitter> connections = 
                (ConcurrentHashMap<Long, SseEmitter>) connectionsField.get(sseService);
            return connections.containsKey(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 모든 연결 제거 (테스트 초기화용)
     */
    public void clearAllConnections() {
        try {
            Field connectionsField = SseService.class.getDeclaredField("connections");
            connectionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<Long, SseEmitter> connections = 
                (ConcurrentHashMap<Long, SseEmitter>) connectionsField.get(sseService);
            connections.clear();
        } catch (Exception e) {
            // 테스트 환경에서 리플렉션 실패는 무시
        }
    }

    /**
     * SSE 연결 상태를 확인하는 헬퍼 메서드
     */
    public boolean waitForConnection(Long userId, int timeoutSeconds) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean connected = new AtomicBoolean(false);

        Thread checkThread = new Thread(() -> {
            try {
                int attempts = 0;
                while (attempts < timeoutSeconds * 10) { // 100ms마다 체크
                    if (isUserConnected(userId)) {
                        connected.set(true);
                        latch.countDown();
                        return;
                    }
                    Thread.sleep(100);
                    attempts++;
                }
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                latch.countDown();
            }
        });

        checkThread.start();
        latch.await(timeoutSeconds, TimeUnit.SECONDS);
        return connected.get();
    }

    /**
     * 알람 전송 결과를 확인하는 헬퍼 메서드
     */
    public AlarmSendResult sendAlarmAndWait(Long userId, Alarm alarm, int timeoutSeconds) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<Exception> exception = new AtomicReference<>();

        Thread sendThread = new Thread(() -> {
            try {
                sseService.sendAlarmToUser(userId, alarm);
                success.set(true);
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        sendThread.start();
        boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);

        return new AlarmSendResult(completed, success.get(), exception.get());
    }

    /**
     * 연결이 제거될 때까지 대기하는 헬퍼 메서드
     */
    public boolean waitForDisconnection(int expectedConnectionCount, int timeoutSeconds) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean disconnected = new AtomicBoolean(false);

        Thread checkThread = new Thread(() -> {
            try {
                int attempts = 0;
                while (attempts < timeoutSeconds * 10) { // 100ms마다 체크
                    if (getActiveConnectionCount() == expectedConnectionCount) {
                        disconnected.set(true);
                        latch.countDown();
                        return;
                    }
                    Thread.sleep(100);
                    attempts++;
                }
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                latch.countDown();
            }
        });

        checkThread.start();
        latch.await(timeoutSeconds, TimeUnit.SECONDS);
        return disconnected.get();
    }

    /**
     * 알람 전송 결과를 담는 클래스
     */
    public static class AlarmSendResult {
        private final boolean completed;
        private final boolean success;
        private final Exception exception;

        public AlarmSendResult(boolean completed, boolean success, Exception exception) {
            this.completed = completed;
            this.success = success;
            this.exception = exception;
        }

        public boolean isCompleted() {
            return completed;
        }

        public boolean isSuccess() {
            return success;
        }

        public Exception getException() {
            return exception;
        }

        public boolean hasFailed() {
            return !success;
        }

        public boolean hasException() {
            return exception != null;
        }
    }
}
