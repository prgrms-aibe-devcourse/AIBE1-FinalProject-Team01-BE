package kr.co.amateurs.server.fixture.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.fixture.common.TestConstants;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DirectMessageFixture {

    // 메시지 내용 상수
    public static final String MESSAGE_CONTENT_1 = "테스트 메시지";
    public static final String MESSAGE_CONTENT_2 = "새로운 메시지";
    public static final String MESSAGE_CONTENT_OLD = "이전 메시지";
    public static final String MESSAGE_CONTENT_BEFORE_LEFT = "나가기 전 메시지";
    public static final String MESSAGE_CONTENT_AFTER_LEFT = "나간 후 메시지";
    public static final String LAST_MESSAGE_1 = "안녕하세요";
    public static final String LAST_MESSAGE_2 = "반갑습니다";
    public static final String UNAUTHORIZED_MESSAGE = "허가되지 않은 메시지";

    private final DirectMessageRepository repository;

    public DirectMessageFixture(DirectMessageRepository repository) {
        this.repository = repository;
    }

    public DirectMessage createAndSaveMessage(String roomId, String content) {
        return createAndSaveMessage(roomId, content, TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
    }

    public DirectMessage createAndSaveMessage(
            String roomId,
            String content,
            Long senderId,
            String senderName
    ) {
        DirectMessage message = createMessage(
                roomId,
                content,
                senderId,
                senderName,
                LocalDateTime.now());
        return repository.save(message);
    }

    public DirectMessage createAndSaveMessage(
            String roomId,
            String content,
            Long senderId,
            String senderName,
            String senderNickName
    ) {
        DirectMessage message = createMessage(
                roomId,
                content,
                senderId,
                senderName,
                LocalDateTime.now());
        return repository.save(message);
    }

    public void createMultipleMessages(String roomId, int count) {
        for (int i = 1; i <= count; i++) {
            DirectMessage message = createMessage(
                    roomId,
                    "메시지 " + i,
                    (long) (i % 2 == 1 ? 1 : 2),
                    i % 2 == 1 ? "user1" : "user2",
                    LocalDateTime.now().plusHours(i));
            repository.save(message);
        }
    }

    public void createMessagesBeforeAndAfterUserLeft(String roomId) {
        LocalDateTime now = LocalDateTime.now();

        DirectMessage oldMessage = createMessage(
                roomId,
                MESSAGE_CONTENT_BEFORE_LEFT,
                TestConstants.USER_ID_2,
                TestConstants.USER_NAME_2,
                now.minusMinutes(30));
        repository.save(oldMessage);

        DirectMessage newMessage = createMessage(
                roomId,
                MESSAGE_CONTENT_AFTER_LEFT,
                TestConstants.USER_ID_2,
                TestConstants.USER_NAME_2,
                now.plusMinutes(30));
        repository.save(newMessage);
    }

    public void createTestMessages(String roomId) {
        createAndSaveMessage(roomId, "테스트 메시지 1", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
        createAndSaveMessage(roomId, "테스트 메시지 2", TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
    }

    private DirectMessage createMessage(String roomId, String content, Long senderId, String senderNickname, LocalDateTime sentAt) {
        return DirectMessage.builder()
                .roomId(roomId)
                .content(content)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .messageType(MessageType.TEXT)
                .sentAt(sentAt)
                .build();
    }
}