package kr.co.amateurs.server.controller.directmessage;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("wss")
@RequiredArgsConstructor
public class TestWebsocketController {
    private final WebSocketMessageBrokerStats stats;

    @GetMapping
    public Map<String, Object> overview() {
        Map<String, Object> m = new HashMap<>();
        m.put("sessionStats", stats.getWebSocketSessionStats());

        return m;
    }
}
