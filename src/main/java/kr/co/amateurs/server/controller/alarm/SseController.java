package kr.co.amateurs.server.controller.alarm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.service.alarm.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Sse", description = "SSE API")
@RestController
@RequestMapping("api/v1/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 연결", description = "실시간 알람을 받기 위한 SSE 연결을 생성합니다.")
    public ResponseEntity<SseEmitter> connect() {
        return ResponseEntity.ok(sseService.connect());
    }

    @DeleteMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 연결 해제", description = "실시간 알람을 받기 위한 SSE 연결을 종료합니다.")
    public ResponseEntity<Void> disconnect() {
        sseService.disconnect();
        return ResponseEntity.noContent().build();
    }
}
