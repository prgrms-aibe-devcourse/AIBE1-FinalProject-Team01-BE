package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.alarm.AlarmDTO;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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

    @Scheduled(fixedRate = DEFAULT_HEARTBEAT_INTERVAL) // 30초
    public void sendHeartbeat() {
        if (connections.isEmpty()) {
            return;
        }

        connections.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("ping"));
            } catch (Exception e) {
                connections.remove(userId);
            }
        });
    }

    public SseEmitter connect() {
        long currentUserId = userService.getCurrentLoginUser().getId();
        connections.remove(currentUserId);

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        connections.put(currentUserId, emitter);

        emitter.onCompletion(() -> connections.remove(currentUserId));
        emitter.onTimeout(() -> connections.remove(currentUserId));
        emitter.onError(e -> connections.remove(currentUserId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully"));
        } catch (IOException e) {
            connections.remove(currentUserId);
        }

        return emitter;
    }

    public void disconnect() {
        long currentUserId = userService.getCurrentLoginUser().getId();
        SseEmitter emitter = connections.remove(currentUserId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    public void sendAlarmToUser(long userId, Alarm alarm) {
        SseEmitter emitter = connections.get(userId);
        if (emitter == null) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        try {
            AlarmDTO alarmDTO = AlarmDTO.from(alarm);
            emitter.send(SseEmitter.event()
                    .id(alarm.getId())
                    .name("alarm")
                    .data(alarmDTO));
        } catch (IOException e) {
            connections.remove(userId);
        }
    }
}