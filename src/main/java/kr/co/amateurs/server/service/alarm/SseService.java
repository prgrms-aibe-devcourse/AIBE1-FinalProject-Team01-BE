package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.domain.dto.alarm.AlarmDTO;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {
    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간
    private static final long DEFAULT_HEARTBEAT_INTERVAL = 30L * 1000; // 30초

    private final ConcurrentHashMap<Long, SseEmitter> connections = new ConcurrentHashMap<>();
    private final UserService userService;

    @Scheduled(fixedRate = DEFAULT_HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        if (connections.isEmpty()) {
            return;
        }

        connections.entrySet().removeIf(entry -> {
            Long userId = entry.getKey();
            SseEmitter emitter = entry.getValue();
            
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("ping"));
                return false; // 정상 전송, 유지
            } catch (IOException e) {
                // 클라이언트 연결 종료 - 정상적인 상황이므로 조용히 처리
                closeEmitterSafely(emitter);
                return true; // 제거
            } catch (Exception e) {
                // 예상치 못한 오류만 로깅
                log.warn("SSE 하트비트 전송 실패: userId={}, error={}", userId, e.getMessage());
                closeEmitterSafely(emitter);
                return true; // 제거
            }
        });
    }

    public SseEmitter connect() {
        long currentUserId = userService.getCurrentLoginUser().getId();
        
        // 기존 연결이 있다면 안전하게 정리
        SseEmitter existingEmitter = connections.remove(currentUserId);
        if (existingEmitter != null) {
            closeEmitterSafely(existingEmitter);
        }

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        connections.put(currentUserId, emitter);

        // 연결 수명주기 이벤트 핸들러 등록
        emitter.onCompletion(() -> connections.remove(currentUserId));
        emitter.onTimeout(() -> connections.remove(currentUserId));
        emitter.onError(throwable -> {
            connections.remove(currentUserId);
            // 예상치 못한 오류만 로깅
            if (!isExpectedConnectionError(throwable)) {
                log.warn("SSE 연결 예상치 못한 오류: userId={}, error={}", 
                        currentUserId, throwable.getMessage());
            }
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully"));
        } catch (IOException e) {
            connections.remove(currentUserId);
            log.error("SSE 초기 연결 메시지 전송 실패: userId={}", currentUserId, e);
            throw new RuntimeException("SSE 연결 실패", e);
        }

        return emitter;
    }

    public void disconnect() {
        long currentUserId = userService.getCurrentLoginUser().getId();
        SseEmitter emitter = connections.remove(currentUserId);
        if (emitter != null) {
            closeEmitterSafely(emitter);
        }
    }

    public void sendAlarmToUser(long userId, Alarm alarm) {
        SseEmitter emitter = connections.get(userId);
        if (emitter == null) {
            return; // 연결 없음 - 조용히 무시
        }

        try {
            AlarmDTO alarmDTO = AlarmDTO.from(alarm);
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(alarm.getId()))
                    .name("alarm")
                    .data(alarmDTO));
        } catch (IOException e) {
            // 클라이언트 연결 종료 - 정상적인 상황이므로 조용히 처리
            connections.remove(userId);
            closeEmitterSafely(emitter);
        } catch (Exception e) {
            // 예상치 못한 오류만 로깅
            log.warn("알람 전송 예상치 못한 오류: userId={}, alarmId={}, error={}", 
                    userId, alarm.getId(), e.getMessage());
            connections.remove(userId);
            closeEmitterSafely(emitter);
        }
    }

    /**
     * SseEmitter를 안전하게 종료하는 유틸리티 메서드
     */
    private void closeEmitterSafely(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // emitter 종료 중 예외는 무시 (이미 종료된 상태일 수 있음)
        }
    }

    /**
     * 예상되는 연결 종료 오류인지 판단
     */
    private boolean isExpectedConnectionError(Throwable throwable) {
        return throwable instanceof IOException ||
               throwable instanceof AsyncRequestNotUsableException ||
               (throwable.getMessage() != null && 
                throwable.getMessage().contains("Broken pipe"));
    }
}
