package kr.co.amateurs.server.controller.alarm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.service.alarm.AlarmFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ALARM API")
@RestController
@RequestMapping("api/v1/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmFacade alarmFacade;

    @PatchMapping
    @Operation(summary = "알람 전체 읽기")
    public ResponseEntity<Void> readAll() {
        alarmFacade.readAll();
        return ResponseEntity.noContent().build();
    }
}
