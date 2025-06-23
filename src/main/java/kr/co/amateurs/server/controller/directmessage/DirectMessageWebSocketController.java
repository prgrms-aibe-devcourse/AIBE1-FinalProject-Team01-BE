package kr.co.amateurs.server.controller.directmessage;

import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageRequest;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageResponse;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageWebSocketController {
    private final DirectMessageService directMessageService;

    @MessageMapping("/dm/room/{roomId}")
    @SendTo("/topic/dm/room/{roomId}")
    public DirectMessageResponse chat(@DestinationVariable String roomId, DirectMessageRequest directMessageRequest) {
        return directMessageService.saveMessage(roomId, directMessageRequest);
    }
}
