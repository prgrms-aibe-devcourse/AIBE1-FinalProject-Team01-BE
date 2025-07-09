package kr.co.amateurs.server.controller.alarm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.alarm.AlarmPageResponse;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.service.alarm.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alarm", description = "실시간 알림 관련 API")
@RestController
@RequestMapping("api/v1/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    @Operation(summary = "알람 목록 조회")
    public ResponseEntity<AlarmPageResponse> readAlarms(@ParameterObject @Valid PaginationParam param) {
        return ResponseEntity.ok(alarmService.readAlarms(param));
    }

    @PostMapping
    @Operation(summary = "테스트용 알람 생성")
    public void createTestAlarm() {
        alarmService.createTestAlarm();
    }

    @PatchMapping
    @Operation(summary = "알람 전체 읽기 처리")
    public ResponseEntity<Void> markAllAsRead() {
        alarmService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("{alarmId}")
    @Operation(summary = "알람 단 건 읽음 처리 ")
    public ResponseEntity<Void> markAsRead(@PathVariable String alarmId) {
        alarmService.markAsRead(alarmId);
        return ResponseEntity.noContent().build();
    }
}
