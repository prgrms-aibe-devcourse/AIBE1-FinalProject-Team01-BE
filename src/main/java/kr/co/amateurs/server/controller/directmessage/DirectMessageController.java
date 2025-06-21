package kr.co.amateurs.server.controller.directmessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DM API")
@RestController
@RequestMapping("api/v1/dm")
@RequiredArgsConstructor
public class DirectMessageController {
    private final DirectMessageService directMessageService;

    @PostMapping
    @Operation(summary = "채팅방 생성",description = "jwt 구현 완료 시 body 수정 예정")
    public ResponseEntity<DirectMessageRoomResponse> createRoom(@RequestBody DirectMessageRoomCreateRequest request) {
        DirectMessageRoomResponse room = directMessageService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @GetMapping("rooms/{userId}")
    @Operation(summary = "채팅방 목록 조회")
    public ResponseEntity<List<DirectMessageRoomResponse>> getRooms(@PathVariable Long userId) {
        List<DirectMessageRoomResponse> rooms = directMessageService.getRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("messages")
    @Operation(summary = "채팅 기록 조회")
    public ResponseEntity<DirectMessagePageResponse> getMessages(@ParameterObject DirectMessagePaginationParam param) {
        DirectMessagePageResponse message = directMessageService.getMessages(param);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping
    @Operation(summary = "방 나가기")
    public ResponseEntity<Void> exitRoom(@RequestBody DirectMessageRoomExitRequest request) {
        directMessageService.exitRoom(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("all")
    @Operation(summary = "채팅 데이터 전체 삭제(개발용)")
    public void deleteAll() {
        directMessageService.deleteAll();
    }
}
