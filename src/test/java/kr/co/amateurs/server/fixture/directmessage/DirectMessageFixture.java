package kr.co.amateurs.server.fixture.directmessage;

import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageSearchPaginationParam;
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
    public static final String MESSAGE_CONTENT_BEFORE_LEFT = "나가기 전 메시지";
    public static final String MESSAGE_CONTENT_AFTER_LEFT = "나간 후 메시지";
    public static final String LAST_MESSAGE_1 = "안녕하세요";
    public static final String LAST_MESSAGE_2 = "반갑습니다";
    // 검색 테스트용 상수 추가
    public static final String SEARCH_KEYWORD_HELLO = "안녕";
    public static final String SEARCH_CONTENT_1 = "안녕하세요 반갑습니다";
    public static final String SEARCH_CONTENT_2 = "안녕히 가세요";
    public static final String SEARCH_CONTENT_3 = "좋은 하루 되세요";
    public static final String SEARCH_CONTENT_EN_1 = "Hello World";
    public static final String SEARCH_CONTENT_EN_2 = "hello everyone";
    public static final String SEARCH_CONTENT_EN_3 = "HELLO THERE";
    public static final String SEARCH_CONTENT_MIXED = "테스트 메시지";
    public static final String SEARCH_REENTRY_BEFORE = "재입장 전 검색 테스트";
    public static final String SEARCH_REENTRY_AFTER = "재입장 후 검색 테스트";

    private final DirectMessageRepository repository;

    public DirectMessageFixture(DirectMessageRepository repository) {
        this.repository = repository;
    }

    public static DirectMessageSearchPaginationParam createSearchPaginationParam(String keyword) {
        return DirectMessageSearchPaginationParam.builder()
                .keyword(keyword)
                .page(0)
                .size(10)
                .build();
    }

    public static DirectMessageSearchPaginationParam createSearchPaginationParam(String keyword, int page, int size) {
        return DirectMessageSearchPaginationParam.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();
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

    // 검색 테스트용 메서드들
    public void createSearchTestMessages(String roomId) {
        createAndSaveMessage(roomId, SEARCH_CONTENT_1, TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
        createAndSaveMessage(roomId, SEARCH_CONTENT_2, TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
        createAndSaveMessage(roomId, SEARCH_CONTENT_3, TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
    }

    public void createEnglishSearchTestMessages(String roomId) {
        createAndSaveMessage(roomId, SEARCH_CONTENT_EN_1, TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
        createAndSaveMessage(roomId, SEARCH_CONTENT_EN_2, TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
        createAndSaveMessage(roomId, SEARCH_CONTENT_EN_3, TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
        createAndSaveMessage(roomId, SEARCH_CONTENT_3, TestConstants.USER_ID_2, TestConstants.USER_NAME_2); // 한글 메시지도 하나 추가
    }

    public void createMultipleRoomSearchMessages(String roomId1, String roomId2) {
        createAndSaveMessage(roomId1, SEARCH_CONTENT_MIXED + " 1", TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
        createAndSaveMessage(roomId1, "다른 내용", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
        createAndSaveMessage(roomId2, SEARCH_CONTENT_MIXED + " 2", TestConstants.USER_ID_3, TestConstants.USER_NAME_3);
        createAndSaveMessage(roomId2, "또 다른 내용", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
    }

    public void createPaginationTestMessages(String roomId, int count) {
        for (int i = 1; i <= count; i++) {
            createAndSaveMessage(roomId, "페이징 테스트 " + i, TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
        }
    }

    public void createTimeSortedMessages(String roomId) {
        LocalDateTime baseTime = LocalDateTime.now();

        DirectMessage oldMessage = createMessage(
                roomId,
                "첫 번째 테스트",
                TestConstants.USER_ID_1,
                TestConstants.USER_NAME_1,
                baseTime.minusHours(2)
        );
        repository.save(oldMessage);

        DirectMessage newMessage = createMessage(
                roomId,
                "두 번째 테스트",
                TestConstants.USER_ID_2,
                TestConstants.USER_NAME_2,
                baseTime.minusHours(1)
        );
        repository.save(newMessage);
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