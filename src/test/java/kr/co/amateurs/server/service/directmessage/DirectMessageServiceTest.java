package kr.co.amateurs.server.service.directmessage;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Import(DirectMessageService.class)
@ActiveProfiles("test")
class DirectMessageServiceTest {

    @Autowired
    private DirectMessageService directMessageService;

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private DirectMessageRoomRepository directMessageRoomRepository;

    @BeforeEach
    void setUp() {
        directMessageRepository.deleteAll();
        directMessageRoomRepository.deleteAll();
    }

    @Nested
    class SaveMessage {

        @Nested
        class 성공 {

            @Test
            void 정상적인_메시지_저장_시_DirectMessageResponse를_반환한다() {
                // given
                String roomId = "room123";
                createAndSaveTestRoom(roomId, 1L, "user1", 2L, "user2");
                DirectMessageRequest request = createMessageRequest("테스트 메시지", 1L, "user1");

                // when
                DirectMessageResponse response = directMessageService.saveMessage(roomId, request);

                // then
                assertThat(response.content()).isEqualTo("테스트 메시지");
                assertThat(response.senderId()).isEqualTo(1L);
                assertThat(response.senderNickname()).isEqualTo("user1");
                assertThat(response.roomId()).isEqualTo(roomId);
                assertThat(response.messageType()).isEqualTo(MessageType.TEXT);

                Optional<DirectMessage> message = directMessageRepository.findById(response.id());
                assertThat(message)
                        .isPresent()
                        .hasValueSatisfying(m -> {
                            assertThat(m.getContent()).isEqualTo(response.content());
                        });
            }

            @Test
            void 메시지_저장_시_채팅방의_마지막_메시지가_업데이트된다() {
                // given
                String roomId = "room456";
                DirectMessageRoom room = createAndSaveTestRoom(roomId, 1L, "user1", 2L, "user2");
                room.updateLastMessage("이전 메시지");
                directMessageRoomRepository.save(room);

                DirectMessageRequest request = createMessageRequest("새로운 메시지", 1L, "user1");

                // when
                directMessageService.saveMessage(roomId, request);

                // then
                DirectMessageRoom updatedRoom = directMessageRoomRepository.findById(roomId).orElseThrow();
                assertThat(updatedRoom.getLastMessage()).isEqualTo(request.content());
            }
        }

        @Nested
        class 실패 {

            @Test
            void 존재하지_않는_채팅방에_메시지_저장_시_NOT_FOUND_ROOM_예외가_발생한다() {
                // given
                String roomId = "nonexistent";
                DirectMessageRequest request = createMessageRequest("테스트 메시지", 1L, "user1");

                // when & then
                assertThatThrownBy(() -> directMessageService.saveMessage(roomId, request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);

                assertThat(directMessageRepository.findAll()).isEmpty();
            }

            @Test
            void 채팅방에_참여하지_않은_사용자가_메시지_저장_시_USER_NOT_IN_ROOM_예외가_발생한다() {
                // given
                String roomId = "room789";
                createAndSaveTestRoom(roomId, 1L, "user1", 2L, "user2");
                DirectMessageRequest request = createMessageRequest("허가되지 않은 메시지", 3L, "user3");

                // when & then
                assertThatThrownBy(() -> directMessageService.saveMessage(roomId, request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.USER_NOT_IN_ROOM);

                assertThat(directMessageRepository.findAll()).isEmpty();
            }
        }
    }

    @Nested
    class CreateRoom {

        @Nested
        class 성공 {

            @Test
            void 새로운_채팅방_생성_시_DirectMessageRoomResponse를_반환한다() {
                // given
                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2"))
                        .build();

                // when
                DirectMessageRoomResponse response = directMessageService.createRoom(request);

                // then
                assertThat(response.roomId()).isNotNull();
                assertThat(response.otherUserId()).isEqualTo(2L);

                Optional<DirectMessageRoom> room = directMessageRoomRepository.findById(response.roomId());

                assertThat(room)
                        .isPresent()
                        .hasValueSatisfying(r -> {
                            assertThat(r.getParticipants()).hasSize(2);
                            assertThat(r.getId()).isEqualTo(response.roomId());
                        });
            }

            @Test
            void 기존_채팅방이_있는_경우_기존_방_정보를_반환한다() {
                // given
                DirectMessageRoom existingRoom = createAndSaveTestRoom("existing_room", 1L, "user1", 2L, "user2");
                existingRoom.updateLastMessage("기존 메시지");
                directMessageRoomRepository.save(existingRoom);

                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2"))
                        .build();

                // when
                DirectMessageRoomResponse response = directMessageService.createRoom(request);

                // then
                assertThat(response.roomId()).isEqualTo("existing_room");
                assertThat(response.lastMessage()).isEqualTo("기존 메시지");
                assertThat(response.otherUserId()).isEqualTo(2L);

                // 중복 생성되지 않았는지 확인
                assertThat(directMessageRoomRepository.findAll()).hasSize(1);
            }

            @Test
            void 비활성화된_참여자가_있는_기존_방의_경우_재입장_처리를_한다() {
                // given
                DirectMessageRoom existingRoom = createAndSaveTestRoom("existing_room", 1L, "user1", 2L, "user2");
                existingRoom.getParticipants().get(0).exitRoom(); // user1 비활성화
                directMessageRoomRepository.save(existingRoom);

                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2"))
                        .build();

                // when
                directMessageService.createRoom(request);

                // then
                DirectMessageRoom updatedRoom = directMessageRoomRepository.findById(existingRoom.getId()).orElseThrow();
                Participant user1 = updatedRoom.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(1L))
                        .findFirst()
                        .orElseThrow();

                assertThat(user1.getIsActive()).isTrue();
            }
        }
    }

    @Nested
    class GetRooms {

        @Nested
        class 성공 {

            @Test
            void 사용자의_활성_채팅방_목록을_반환한다() {
                // given
                Long userId = 1L;
                createAndSaveTestRoomWithMessage("room1", 1L, "user1", 2L, "user2", "안녕하세요");
                createAndSaveTestRoomWithMessage("room2", 1L, "user1", 3L, "user3", "반갑습니다");

                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(userId);

                // then
                assertThat(responses).hasSize(2);
                assertThat(responses.get(0).roomId()).isEqualTo("room1");
                assertThat(responses.get(0).otherUserId()).isEqualTo(2L);
                assertThat(responses.get(0).lastMessage()).isEqualTo("안녕하세요");

                assertThat(responses.get(1).roomId()).isEqualTo("room2");
                assertThat(responses.get(1).otherUserId()).isEqualTo(3L);
                assertThat(responses.get(1).lastMessage()).isEqualTo("반갑습니다");
            }

            @Test
            void 채팅방이_없는_사용자의_경우_빈_목록을_반환한다() {
                // given
                Long userId = 999L;

                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(userId);

                // then
                assertThat(responses).isEmpty();
            }

            @Test
            void 비활성화된_참여자는_채팅방_목록에서_제외된다() {
                // given
                Long userId = 1L;
                DirectMessageRoom room = createAndSaveTestRoom("room1", 1L, "user1", 2L, "user2");
                room.getParticipants().get(0).exitRoom();
                directMessageRoomRepository.save(room);

                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(userId);

                // then
                assertThat(responses).isEmpty();
            }
        }
    }

    @Nested
    class GetMessages {

        @Nested
        class 성공 {

            @Test
            void 채팅방의_메시지_목록을_페이지네이션으로_반환한다() {
                // given
                createAndSaveTestRoom("room1", 1L, "user1", 2L, "user2");

                for (int i = 1; i <= 15; i++) {
                    DirectMessage message = DirectMessage.builder()
                            .roomId("room1")
                            .content("메시지 " + i)
                            .senderId((long) (i % 2 == 1 ? 1 : 2))
                            .senderNickname(i % 2 == 1 ? "user1" : "user2")
                            .messageType(MessageType.TEXT)
                            .sentAt(LocalDateTime.now())
                            .build();
                    directMessageRepository.save(message);
                }

                DirectMessagePaginationParam param = createPaginationParam("room1", 1L, 0, 10);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.messages()).hasSize(10);
                assertThat(response.pageInfo().getTotalElements()).isEqualTo(15);
                assertThat(response.pageInfo().getTotalPages()).isEqualTo(2);
                assertThat(response.pageInfo().getPageNumber()).isZero();

                // 최신 메시지가 먼저 오는지 확인 (DESC 정렬)
                assertThat(response.messages().get(0).content()).isEqualTo("메시지 15");
                assertThat(response.messages().get(9).content()).isEqualTo("메시지 6");
            }

            @Test
            void 사용자가_나간_시점_이후의_메시지만_조회한다() {
                // given
                DirectMessageRoom room = createAndSaveTestRoom("room1", 1L, "user1", 2L, "user2");

                room.getParticipants().get(0).exitRoom();
                directMessageRoomRepository.save(room);

                DirectMessage oldMessage = DirectMessage.builder()
                        .roomId("room1")
                        .content("나가기 전 메시지")
                        .senderId(2L)
                        .senderNickname("user2")
                        .messageType(MessageType.TEXT)
                        .sentAt(LocalDateTime.now().minusMinutes(30))
                        .build();
                directMessageRepository.save(oldMessage);

                DirectMessage newMessage = DirectMessage.builder()
                        .roomId("room1")
                        .content("나간 후 메시지")
                        .senderId(2L)
                        .senderNickname("user2")
                        .messageType(MessageType.TEXT)
                        .sentAt(LocalDateTime.now().plusMinutes(30))
                        .build();
                directMessageRepository.save(newMessage);

                DirectMessagePaginationParam param = createPaginationParam("room1", 1L, 0, 10);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.messages()).hasSize(1);
                assertThat(response.messages().get(0).content()).isEqualTo(newMessage.getContent());
            }

            @Test
            void 메시지가_없는_채팅방의_경우_빈_페이지를_반환한다() {
                // given
                createAndSaveTestRoom("empty_room", 1L, "user1", 2L, "user2");
                DirectMessagePaginationParam param = createPaginationParam("empty_room", 1L, 0, 10);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.messages()).isEmpty();
                assertThat(response.pageInfo().getTotalElements()).isZero();
                assertThat(response.pageInfo().getTotalPages()).isZero();
            }
        }

        @Nested
        class 실패 {

            @Test
            void 존재하지_않는_채팅방의_메시지_조회_시_NOT_FOUND_ROOM_예외가_발생한다() {
                // given
                DirectMessagePaginationParam param = createPaginationParam("nonexistent", 1L, 0, 10);

                // when & then
                assertThatThrownBy(() -> directMessageService.getMessages(param))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);
            }

            @Test
            void 채팅방에_참여하지_않은_사용자가_메시지_조회_시_USER_NOT_IN_ROOM_예외가_발생한다() {
                // given
                createAndSaveTestRoom("room1", 1L, "user1", 2L, "user2");
                DirectMessagePaginationParam param = createPaginationParam("room1", 3L, 0, 10); // user3가 조회

                // when & then
                assertThatThrownBy(() -> directMessageService.getMessages(param))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.USER_NOT_IN_ROOM);
            }
        }
    }

    @Nested
    class ExitRoom {

        @Nested
        class 성공 {

            @Test
            void 정상적인_방_나가기_시_참여자_상태가_비활성화된다() {
                // given
                createAndSaveTestRoom("room123", 1L, "user1", 2L, "user2");
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest("room123", 1L);

                // when
                directMessageService.exitRoom(request);

                // then
                DirectMessageRoom updatedRoom = directMessageRoomRepository.findById("room123").orElseThrow();

                // user1은 비활성 상태, user2는 여전히 활성 상태
                Participant user1 = updatedRoom.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(1L))
                        .findFirst()
                        .orElseThrow();
                assertThat(user1.getIsActive()).isFalse();
                assertThat(user1.getLeftAt()).isNotNull();

                Participant user2 = updatedRoom.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(2L))
                        .findFirst()
                        .orElseThrow();
                assertThat(user2.getIsActive()).isTrue();

                assertThat(directMessageRoomRepository.findById(updatedRoom.getId())).isPresent();
            }

            @Test
            void 모든_참여자가_나간_경우_채팅방과_메시지가_삭제된다() {
                // given
                String roomId = "room456";
                createAndSaveTestRoom(roomId, 1L, "user1", 2L, "user2");

                DirectMessage message1 = DirectMessage.builder()
                        .roomId(roomId)
                        .content("테스트 메시지 1")
                        .senderId(1L)
                        .senderNickname("user1")
                        .messageType(MessageType.TEXT)
                        .sentAt(LocalDateTime.now())
                        .build();
                DirectMessage message2 = DirectMessage.builder()
                        .roomId(roomId)
                        .content("테스트 메시지 2")
                        .senderId(2L)
                        .senderNickname("user2")
                        .messageType(MessageType.TEXT)
                        .sentAt(LocalDateTime.now())
                        .build();
                directMessageRepository.saveAll(List.of(message1, message2));

                // when - 두 사용자 모두 나가기
                directMessageService.exitRoom(new DirectMessageRoomExitRequest(roomId, 1L));
                directMessageService.exitRoom(new DirectMessageRoomExitRequest(roomId, 2L));

                // then
                assertThat(directMessageRoomRepository.findById(roomId)).isEmpty();
                List<DirectMessage> remainingMessages = directMessageRepository.findAll();
                assertThat(remainingMessages).isEmpty();
            }

            @Test
            void 한_명이_나간_후_다시_참여할_수_있다() {
                // given
                createAndSaveTestRoom("room789", 1L, "user1", 2L, "user2");
                DirectMessageRoomExitRequest exitRequest = new DirectMessageRoomExitRequest("room789", 1L);

                // when - user1이 나감
                directMessageService.exitRoom(exitRequest);

                // then - user1이 비활성화됨
                DirectMessageRoom updatedRoom = directMessageRoomRepository.findById("room789").orElseThrow();
                Participant user1 = updatedRoom.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(1L))
                        .findFirst()
                        .orElseThrow();
                assertThat(user1.getIsActive()).isFalse();

                // when - user1이 다시 참여
                DirectMessageRoomCreateRequest createRequest = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2"))
                        .build();
                directMessageService.createRoom(createRequest);

                // then - user1이 다시 활성화됨
                DirectMessageRoom reJoinedRoom = directMessageRoomRepository.findById("room789").orElseThrow();
                Participant reJoinedUser1 = reJoinedRoom.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(1L))
                        .findFirst()
                        .orElseThrow();
                assertThat(reJoinedUser1.getIsActive()).isTrue();
            }
        }

        @Nested
        class 실패 {

            @Test
            @DisplayName("존재하지 않는 채팅방에서 나가기 시 NOT_FOUND_ROOM 예외가 발생한다")
            void not_found_room() {
                // given
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest("nonexistent", 1L);

                // when & then
                assertThatThrownBy(() -> directMessageService.exitRoom(request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);
            }

            @Test
            @DisplayName("참여하지 않은 사용자가 방 나가기 시 USER_NOT_IN_ROOM 예외가 발생한다")
            void user_not_in_room() {
                // given
                createAndSaveTestRoom("room789", 1L, "user1", 2L, "user2");
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest("room789", 999L);

                // when & then
                assertThatThrownBy(() -> directMessageService.exitRoom(request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.USER_NOT_IN_ROOM);
            }
        }
    }

    private DirectMessageRoom createAndSaveTestRoom(String roomId, Long user1Id, String user1Name, Long user2Id, String user2Name) {
        DirectMessageRoom room = DirectMessageRoom.builder()
                .id(roomId)
                .participants(List.of(
                        Participant.builder()
                                .userId(user1Id)
                                .nickname(user1Name)
                                .isActive(true)
                                .build(),
                        Participant.builder()
                                .userId(user2Id)
                                .nickname(user2Name)
                                .isActive(true)
                                .build()
                ))
                .build();
        return directMessageRoomRepository.save(room);
    }

    private DirectMessageRoom createAndSaveTestRoomWithMessage(
            String roomId,
            Long user1Id,
            String user1Name,
            Long user2Id,
            String user2Name,
            String lastMessage) {
        DirectMessageRoom room = createAndSaveTestRoom(roomId, user1Id, user1Name, user2Id, user2Name);
        room.updateLastMessage(lastMessage);
        return directMessageRoomRepository.save(room);
    }

    private DirectMessageRequest createMessageRequest(String content, Long senderId, String senderName) {
        return DirectMessageRequest.builder()
                .content(content)
                .senderId(senderId)
                .senderName(senderName)
                .messageType(MessageType.TEXT)
                .build();
    }

    private DirectMessagePaginationParam createPaginationParam(String roomId, Long userId, int page, int size) {
        return DirectMessagePaginationParam.builder()
                .roomId(roomId)
                .userId(userId)
                .page(page)
                .size(size)
                .field(PaginationSortType.DM_SENT_AT)
                .sortDirection(Sort.Direction.DESC)
                .build();
    }
}