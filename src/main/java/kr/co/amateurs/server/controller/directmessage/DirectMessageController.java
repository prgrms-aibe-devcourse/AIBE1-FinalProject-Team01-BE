package kr.co.amateurs.server.controller.directmessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DM", description = "DM 관련 API")
@RestController
@RequestMapping("api/v1/dm")
@RequiredArgsConstructor
public class DirectMessageController {
    private final DirectMessageService directMessageService;

    @PostMapping("{partnerId}")
    @Operation(summary = "채팅방 생성")
    public ResponseEntity<DirectMessageRoomResponse> createRoom(@PathVariable long partnerId) {
        DirectMessageRoomResponse room = directMessageService.createRoom(partnerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @GetMapping("rooms")
    @Operation(summary = "채팅방 목록 조회")
    public ResponseEntity<List<DirectMessageRoomResponse>> getRooms() {
        List<DirectMessageRoomResponse> rooms = directMessageService.getRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("messages")
    @Operation(summary = "채팅 기록 조회")
    public ResponseEntity<DirectMessagePageResponse> getMessages(@Valid @ParameterObject DirectMessagePaginationParam param) {
        DirectMessagePageResponse message = directMessageService.getMessages(param);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("{roomId}")
    @Operation(summary = "방 나가기")
    public ResponseEntity<Void> exitRoom(@PathVariable String roomId) {
        directMessageService.exitRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
