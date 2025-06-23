package kr.co.amateurs.server.controller.directmessage;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;

public class TestStompFrameHandler<T> implements StompFrameHandler {

    private final BlockingQueue<T> messageQueue;
    private final Class<T> targetClass;

    public TestStompFrameHandler(BlockingQueue<T> messageQueue, Class<T> targetClass) {
        this.messageQueue = messageQueue;
        this.targetClass = targetClass;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return targetClass;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        boolean offered = messageQueue.offer((T) payload);
    }
}
