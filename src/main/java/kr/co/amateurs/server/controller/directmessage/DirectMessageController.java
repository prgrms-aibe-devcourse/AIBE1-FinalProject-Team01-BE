package kr.co.amateurs.server.controller.directmessage;

import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageRequest;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageController {

    @MessageMapping("/dm/room/{roomId}")
    @SendTo("/topic/dm/room/{roomId}")
    public DirectMessageResponse chat(@DestinationVariable Long roomId, DirectMessageRequest directMessageRequest) {
        return new DirectMessageResponse(
                UUID.randomUUID().toString(),
                directMessageRequest.content(),
                directMessageRequest.senderId(),
                directMessageRequest.messageType()
        );
    }
}
