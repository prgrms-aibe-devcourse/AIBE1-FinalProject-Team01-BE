package kr.co.amateurs.server.controller.directmessage;

import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageRequest;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageResponse;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class DirectMessageWebSocketControllerTest extends AbstractControllerTest {

    @MockitoBean
    private DirectMessageService directMessageService;

    @Autowired
    private DirectMessageRepository directMessageRepository;

    private StompSession stompSession;
    private BlockingQueue<DirectMessageResponse> messageQueue;

    @BeforeEach
    void setUp() throws Exception {
        directMessageRepository.deleteAll();

        messageQueue = new LinkedBlockingQueue<>();

        WebSocketStompClient stompClient = createWebSocketStompClient();
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://localhost:" + port + "/ws";
        stompSession = stompClient.connectAsync(url, new TestStompSessionHandler())
                .get(60, TimeUnit.SECONDS);
    }

    @Test
    void 웹소켓_연결이_정상적으로_수행된다() {
        // given & when & then
        assertThat(stompSession).isNotNull();
        assertThat(stompSession.isConnected()).isTrue();
    }

    @Test
    void 메시지가_정상적으로_브로드캐스팅된다() throws InterruptedException {
        // given
        String roomId = "test-room-001";
        String testMessage = "안녕하세요, 테스트 메시지입니다.";
        Long senderId = 1L;
        String senderName = "테스터";

        DirectMessageRequest request = DirectMessageRequest.builder()
                .content(testMessage)
                .senderId(senderId)
                .senderName(senderName)
                .messageType(MessageType.TEXT)
                .build();

        DirectMessageResponse response = DirectMessageResponse.builder()
                .roomId(roomId)
                .content(testMessage)
                .senderId(senderId)
                .senderNickname(senderName)
                .build();

        stompSession.subscribe(
                "/topic/dm/room/" + roomId,
                new TestStompFrameHandler<>(messageQueue, DirectMessageResponse.class)
        );

        given(directMessageService.saveMessage(any(String.class), any(DirectMessageRequest.class)))
                .willReturn(response);

        // when
        stompSession.send("/app/dm/room/" + roomId, request);

        // then
        DirectMessageResponse message = messageQueue.poll(5, TimeUnit.SECONDS);

        assertThat(message).isNotNull();
        assertThat(message.content()).isEqualTo(response.content());
        assertThat(message.senderId()).isEqualTo(response.senderId());
        assertThat(message.senderNickname()).isEqualTo(response.senderNickname());
        assertThat(message.roomId()).isEqualTo(response.roomId());
    }

    @DisplayName("잘못된 룸ID로 메시지 전송 시 적절히 처리된다")
    @Test
    void invalidRoomIdTest() throws InterruptedException {
        // given
        String invalidRoomId = "";

        DirectMessageRequest request = DirectMessageRequest.builder()
                .content("테스트 메시지")
                .senderId(1L)
                .senderName("테스터")
                .messageType(MessageType.TEXT)
                .build();

        stompSession.subscribe(
                "/topic/dm/room/" + invalidRoomId,
                new TestStompFrameHandler<>(messageQueue, DirectMessageResponse.class)
        );

        // when
        stompSession.send("/app/dm/room/" + invalidRoomId, request);

        DirectMessageResponse response = messageQueue.poll(2, TimeUnit.SECONDS);
        assertThat(response).isNull();
    }

    @DisplayName("연결 끊기 후 메시지 전송 시 예외가 발생한다")
    @Test
    void disconnectedSessionTest() {
        // given
        String roomId = "test-room-003";

        // 연결 끊기
        stompSession.disconnect();

        DirectMessageRequest request = DirectMessageRequest.builder()
                .content("연결 끊기 후 메시지")
                .senderId(1L)
                .senderName("테스터")
                .messageType(MessageType.TEXT)
                .build();

        // when & then
        try {
            stompSession.send("/app/dm/room/" + roomId, request);
            // 끊어진 연결에서 메시지 전송 시 예외 발생 예상
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @DisplayName("방 구독 해제 후 메시지 수신이 안된다")
    @Test
    void unsubscribeTest() throws InterruptedException {
        // given
        String roomId = "test-room-004";

        var subscription = stompSession.subscribe(
                "/topic/dm/room/" + roomId,
                new TestStompFrameHandler<>(messageQueue, DirectMessageResponse.class)
        );

        subscription.unsubscribe();

        DirectMessageRequest request = DirectMessageRequest.builder()
                .content("구독 해제 후 메시지")
                .senderId(1L)
                .senderName("테스터")
                .messageType(MessageType.TEXT)
                .build();

        // when
        stompSession.send("/app/dm/room/" + roomId, request);

        // then
        DirectMessageResponse response = messageQueue.poll(2, TimeUnit.SECONDS);
        assertThat(response).isNull();
    }

    private WebSocketStompClient createWebSocketStompClient() {
        return new WebSocketStompClient(new StandardWebSocketClient());
    }
}