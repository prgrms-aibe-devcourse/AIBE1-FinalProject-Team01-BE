package kr.co.amateurs.server.repository.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class DirectMessageRepositoryTest {

    @Autowired
    private DirectMessageRepository directMessageRepository;

    private String testRoomId;
    private DirectMessage testMessage1;
    private DirectMessage testMessage2;
    private DirectMessage testMessage3;

    @BeforeEach
    void setUp() {
        testRoomId = "testRoom123";

        testMessage1 = DirectMessage.builder()
                .roomId(testRoomId)
                .senderId(1L)
                .senderNickname("user1")
                .content("첫 번째 메시지")
                .messageType(MessageType.TEXT)
                .sentAt(LocalDateTime.now().minusHours(3))
                .build();

        testMessage2 = DirectMessage.builder()
                .roomId(testRoomId)
                .senderId(2L)
                .senderNickname("user2")
                .content("두 번째 메시지")
                .messageType(MessageType.TEXT)
                .sentAt(LocalDateTime.now().minusHours(2))
                .build();

        testMessage3 = DirectMessage.builder()
                .roomId(testRoomId)
                .senderId(1L)
                .senderNickname("user1")
                .content("세 번째 메시지")
                .messageType(MessageType.TEXT)
                .sentAt(LocalDateTime.now().minusHours(1))
                .build();

        directMessageRepository.deleteAll();
        directMessageRepository.saveAll(List.of(testMessage1, testMessage2, testMessage3));
    }

    @Test
    @DisplayName("채팅방_ID로_메시지_조회_시_최신순으로_정렬되어_반환")
    void findByRoomIdOrderBySentAtDesc() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<DirectMessage> result = directMessageRepository.findByRoomIdOrderBySentAtDesc(testRoomId, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("세 번째 메시지");
        assertThat(result.getContent().get(1).getContent()).isEqualTo("두 번째 메시지");
        assertThat(result.getContent().get(2).getContent()).isEqualTo("첫 번째 메시지");
    }

    @Test
    @DisplayName("특정_시간_이후_메시지_조회_시_해당_조건에_맞는_메시지만_반환되어야_한다")
    void findByRoomIdAndSentAtAfterOrderBySentAtDesc() {
        // given
        LocalDateTime afterTime = LocalDateTime.now().minusHours(2).minusMinutes(30);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<DirectMessage> result = directMessageRepository.findByRoomIdAndSentAtAfterOrderBySentAtDesc(
                testRoomId, afterTime, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("세 번째 메시지");
        assertThat(result.getContent().get(1).getContent()).isEqualTo("두 번째 메시지");
    }
}
