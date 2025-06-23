package kr.co.amateurs.server.repository.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class DirectMessageRoomRepositoryTest {

    @Autowired
    private DirectMessageRoomRepository directMessageRoomRepository;

    private DirectMessageRoom testRoom1;
    private DirectMessageRoom testRoom2;
    private DirectMessageRoom testRoom3;

    @BeforeEach
    void setUp() {
        directMessageRoomRepository.deleteAll();

        // 사용자 1과 2의 채팅방 (둘 다 활성)
        testRoom1 = DirectMessageRoom.builder()
                .lastMessage("안녕하세요")
                .participants(List.of(
                        Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                        Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                ))
                .build();

        // 사용자 1과 3의 채팅방 (사용자 1은 비활성)
        testRoom2 = DirectMessageRoom.builder()
                .lastMessage("반갑습니다")
                .participants(List.of(
                        Participant.builder()
                                .userId(1L)
                                .nickname("user1")
                                .isActive(false)
                                .leftAt(LocalDateTime.now())
                                .build(),
                        Participant.builder().userId(3L).nickname("user3").isActive(true).build()
                ))
                .build();

        // 사용자 2와 3의 채팅방 (둘 다 활성)
        testRoom3 = DirectMessageRoom.builder()
                .lastMessage("좋은 하루!")
                .participants(List.of(
                        Participant.builder().userId(2L).nickname("user2").isActive(true).build(),
                        Participant.builder().userId(3L).nickname("user3").isActive(true).build()
                ))
                .build();

        directMessageRoomRepository.saveAll(List.of(testRoom1, testRoom2, testRoom3));
    }

    @Nested
    class findRoomByUserIds {

        @Test
        void 두_사용자_ID로_채팅방_조회_시_해당_사용자들의_방이_반환되어야_한다() {
            // when
            Optional<DirectMessageRoom> result = directMessageRoomRepository.findRoomByUserIds(1L, 2L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getLastMessage()).isEqualTo("안녕하세요");
            assertThat(result.get().getParticipants()).hasSize(2);
            assertThat(result.get().getParticipants())
                    .extracting(Participant::getUserId)
                    .containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        void 존재하지_않는_사용자_조합으로_채팅방_조회_시_빈_결과가_반환되어야_한다() {
            // when
            Optional<DirectMessageRoom> result = directMessageRoomRepository.findRoomByUserIds(1L, 4L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findActiveRoomsByUserId {

        @Test
        void 사용자_ID로_활성_채팅방_목록_조회_시_해당_사용자가_참여중인_활성_방만_반환되어야_한다() {
            // when
            List<DirectMessageRoom> result = directMessageRoomRepository.findActiveRoomsByUserId(1L);

            // then
            assertThat(result).hasSize(1); // testRoom1만 (사용자1이 활성 상태인 방)
            assertThat(result.get(0).getLastMessage()).isEqualTo("안녕하세요");

            // 사용자1이 활성 상태인지 확인
            boolean isUser1Active = result.get(0).getParticipants().stream()
                    .filter(p -> p.getUserId().equals(1L))
                    .findFirst()
                    .map(Participant::getIsActive)
                    .orElse(false);
            assertThat(isUser1Active).isTrue();
        }

        @Test
        void 사용자2의_활성_채팅방_목록_조회_시_참여중인_모든_활성_방이_반환되어야_한다() {
            // when
            List<DirectMessageRoom> result = directMessageRoomRepository.findActiveRoomsByUserId(2L);

            // then
            assertThat(result).hasSize(2); // testRoom1, testRoom3
            assertThat(result).extracting(DirectMessageRoom::getLastMessage)
                    .containsExactlyInAnyOrder("안녕하세요", "좋은 하루!");
        }

        @Test
        void 참여하지_않은_사용자의_활성_채팅방_조회_시_빈_결과가_반환되어야_한다() {
            // when
            List<DirectMessageRoom> result = directMessageRoomRepository.findActiveRoomsByUserId(4L);

            // then
            assertThat(result).isEmpty();
        }
    }

}
