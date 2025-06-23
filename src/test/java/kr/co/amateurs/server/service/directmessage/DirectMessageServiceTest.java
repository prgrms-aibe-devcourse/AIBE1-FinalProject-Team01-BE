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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessage Service 테스트")
class DirectMessageServiceTest {

    @Mock
    private DirectMessageRepository directMessageRepository;

    @Mock
    private DirectMessageRoomRepository directMessageRoomRepository;

    @InjectMocks
    private DirectMessageService directMessageService;

    @Nested
    @DisplayName("메시지_저장")
    class saveMessage {

        @Nested
        class 성공 {

            @Test
            void 정상적인_메시지_저장_시_응답이_반환() {
                // given
                String roomId = "room123";
                DirectMessageRequest request = DirectMessageRequest.builder()
                        .content("테스트 메시지")
                        .senderId(1L)
                        .senderName("user1")
                        .messageType(MessageType.TEXT)
                        .build();

                DirectMessageRoom room = DirectMessageRoom.builder()
                        .id(roomId)
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").build(),
                                Participant.builder().userId(2L).nickname("user2").build()
                        ))
                        .build();

                DirectMessage savedMessage = DirectMessage.builder()
                        .id("msg123")
                        .roomId(roomId)
                        .content("테스트 메시지")
                        .senderId(1L)
                        .senderNickname("user1")
                        .messageType(MessageType.TEXT)
                        .sentAt(LocalDateTime.now())
                        .build();

                given(directMessageRoomRepository.findById(roomId)).willReturn(Optional.of(room));
                given(directMessageRepository.save(any(DirectMessage.class))).willReturn(savedMessage);
                given(directMessageRoomRepository.save(any(DirectMessageRoom.class))).willReturn(room);

                // when
                DirectMessageResponse response = directMessageService.saveMessage(roomId, request);

                // then
                assertThat(response.id()).isEqualTo("msg123");
                assertThat(response.content()).isEqualTo("테스트 메시지");
                assertThat(response.senderId()).isEqualTo(1L);
                assertThat(response.senderName()).isEqualTo("user1");

                verify(directMessageRoomRepository).findById(roomId);
                verify(directMessageRepository).save(any(DirectMessage.class));
                verify(directMessageRoomRepository).save(any(DirectMessageRoom.class));
            }
        }

        @Nested
        class 실패 {

            @Test
            void 존재하지_않는_채팅방에_메시지_저장_시_예외_발생() {
                // given
                String roomId = "nonexistent";
                DirectMessageRequest request = DirectMessageRequest.builder()
                        .content("테스트 메시지")
                        .senderId(1L)
                        .senderName("user1")
                        .messageType(MessageType.TEXT)
                        .build();

                given(directMessageRoomRepository.findById(roomId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> directMessageService.saveMessage(roomId, request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);

                verify(directMessageRoomRepository).findById(roomId);
                verify(directMessageRepository, never()).save(any());
                verify(directMessageRoomRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("채팅방_생성")
    class createRoom {

        @Nested
        class 성공 {

            @Test
            void 새로운_채팅방_생성_시_정상_생성() {
                // given
                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2"))
                        .build();

                DirectMessageRoom newRoom = DirectMessageRoom.builder()
                        .id("room123")
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                        ))
                        .build();

                given(directMessageRoomRepository.findRoomByUserIds(1L, 2L)).willReturn(Optional.empty());
                given(directMessageRoomRepository.save(any(DirectMessageRoom.class))).willReturn(newRoom);

                // when
                DirectMessageRoomResponse response = directMessageService.createRoom(request);

                // then
                assertThat(response.roomId()).isEqualTo("room123");
                assertThat(response.otherUserId()).isEqualTo(2L);

                verify(directMessageRoomRepository).findRoomByUserIds(1L, 2L);
                verify(directMessageRoomRepository).save(any(DirectMessageRoom.class));
            }

            @Test
            void 기존_채팅방이_있는_경우_기존_방_정보_반환() {
                // given
                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2"))
                        .build();

                DirectMessageRoom existingRoom = DirectMessageRoom.builder()
                        .id("existing_room")
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                        ))
                        .lastMessage("마지막 메시지")
                        .build();

                given(directMessageRoomRepository.findRoomByUserIds(1L, 2L)).willReturn(Optional.of(existingRoom));

                // when
                DirectMessageRoomResponse response = directMessageService.createRoom(request);

                // then
                assertThat(response.roomId()).isEqualTo("existing_room");
                assertThat(response.lastMessage()).isEqualTo("마지막 메시지");
                assertThat(response.otherUserId()).isEqualTo(2L);

                verify(directMessageRoomRepository).findRoomByUserIds(1L, 2L);
                verify(directMessageRoomRepository, never()).save(any(DirectMessageRoom.class));
            }
        }
    }

    @Nested
    @DisplayName("채팅방_목록_조회")
    class getRooms {

        @Nested
        class 성공 {

            @Test
            void 사용자의_채팅방_목록을_정상_조회() {
                // given
                Long userId = 1L;
                List<DirectMessageRoom> rooms = List.of(
                        DirectMessageRoom.builder()
                                .id("room1")
                                .participants(List.of(
                                        Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                        Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                                ))
                                .lastMessage("안녕하세요")
                                .build(),
                        DirectMessageRoom.builder()
                                .id("room2")
                                .participants(List.of(
                                        Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                        Participant.builder().userId(3L).nickname("user3").isActive(true).build()
                                ))
                                .lastMessage("반갑습니다")
                                .build()
                );

                given(directMessageRoomRepository.findActiveRoomsByUserId(userId)).willReturn(rooms);

                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(userId);

                // then
                assertThat(responses).hasSize(2);
                assertThat(responses.get(0).roomId()).isEqualTo(rooms.get(0).getId());
                assertThat(responses.get(0).otherUserId()).isEqualTo(2L);
                assertThat(responses.get(1).roomId()).isEqualTo(rooms.get(1).getId());
                assertThat(responses.get(1).otherUserId()).isEqualTo(3L);

                verify(directMessageRoomRepository).findActiveRoomsByUserId(userId);
            }

            @Test
            void 채팅방이_없는_사용자의_경우_빈_목록_반환() {
                // given
                Long userId = 999L;
                given(directMessageRoomRepository.findActiveRoomsByUserId(userId)).willReturn(List.of());

                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(userId);

                // then
                assertThat(responses).isEmpty();
                verify(directMessageRoomRepository).findActiveRoomsByUserId(userId);
            }
        }
    }

    @Nested
    @DisplayName("메시지_목록_조회")
    class getMessages {

        @Nested
        class 성공 {

            @Test
            void 채팅방의_메시지_목록을_정상_조회() {
                // given
                DirectMessageRoom room = DirectMessageRoom.builder()
                        .id("room1")
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                        ))
                        .lastMessage("안녕하세요")
                        .build();

                DirectMessagePaginationParam param = DirectMessagePaginationParam.builder()
                        .roomId(room.getId())
                        .userId(1L)
                        .page(0)
                        .size(10)
                        .field(PaginationSortType.DM_SENT_AT)
                        .sortDirection(Sort.Direction.DESC)
                        .build();

                List<DirectMessage> messages = List.of(
                        DirectMessage.builder()
                                .id("msg1")
                                .content("첫 번째 메시지")
                                .senderId(1L)
                                .senderNickname("user1")
                                .messageType(MessageType.TEXT)
                                .sentAt(LocalDateTime.now())
                                .build(),
                        DirectMessage.builder()
                                .id("msg2")
                                .content("두 번째 메시지")
                                .senderId(2L)
                                .senderNickname("user2")
                                .messageType(MessageType.TEXT)
                                .sentAt(LocalDateTime.now())
                                .build()
                );

                Page<DirectMessage> page = new PageImpl<>(messages, PageRequest.of(0, 10), 2);
                given(directMessageRoomRepository.findById(any(String.class))).willReturn(Optional.of(room));
                given(directMessageRepository.findByRoomIdOrderBySentAtDesc(eq(room.getId()), any(Pageable.class)))
                        .willReturn(page);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.messages()).hasSize(messages.size());
                assertThat(response.messages().get(0).id()).isEqualTo(messages.get(0).getId());
                assertThat(response.messages().get(1).id()).isEqualTo(messages.get(1).getId());
                assertThat(response.pageInfo().getTotalElements()).isEqualTo(2L);

                verify(directMessageRepository).findByRoomIdOrderBySentAtDesc(eq(room.getId()), any(Pageable.class));
            }

            @Test
            void 메시지가_없는_채팅방의_경우_빈_목록_반환() {
                // given
                DirectMessageRoom room = DirectMessageRoom.builder()
                        .id("room1")
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                        ))
                        .lastMessage("안녕하세요")
                        .build();

                DirectMessagePaginationParam param = DirectMessagePaginationParam.builder()
                        .roomId(room.getId())
                        .userId(1L)
                        .page(0)
                        .size(10)
                        .field(PaginationSortType.DM_SENT_AT)
                        .sortDirection(Sort.Direction.DESC)
                        .build();

                Page<DirectMessage> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
                given(directMessageRoomRepository.findById(any(String.class))).willReturn(Optional.ofNullable(room));
                given(directMessageRepository.findByRoomIdOrderBySentAtDesc(eq(room.getId()), any(Pageable.class)))
                        .willReturn(emptyPage);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.messages()).isEmpty();
                assertThat(response.pageInfo().getTotalElements()).isEqualTo(emptyPage.getTotalElements());

                verify(directMessageRepository).findByRoomIdOrderBySentAtDesc(eq(room.getId()), any(Pageable.class));
            }

            @Test
            void 페이지네이션이_정상_작동() {
                // given
                DirectMessageRoom room = DirectMessageRoom.builder()
                        .id("room1")
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                        ))
                        .lastMessage("안녕하세요")
                        .build();

                DirectMessagePaginationParam param = DirectMessagePaginationParam.builder()
                        .roomId(room.getId())
                        .userId(1L)
                        .page(1)
                        .size(5)
                        .field(PaginationSortType.DM_SENT_AT)
                        .sortDirection(Sort.Direction.DESC)
                        .build();

                List<DirectMessage> messages = List.of(
                        DirectMessage.builder().id("msg6").content("6번째 메시지").build(),
                        DirectMessage.builder().id("msg7").content("7번째 메시지").build()
                );

                Page<DirectMessage> page = new PageImpl<>(messages, PageRequest.of(1, 5), 12);
                given(directMessageRoomRepository.findById(any(String.class))).willReturn(Optional.ofNullable(room));
                given(directMessageRepository.findByRoomIdOrderBySentAtDesc(eq(room.getId()), any(Pageable.class)))
                        .willReturn(page);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.pageInfo().getPageNumber()).isEqualTo(page.getNumber());
                assertThat(response.pageInfo().getPageSize()).isEqualTo(messages.size());
                assertThat(response.pageInfo().getTotalPages()).isEqualTo(page.getTotalPages());
                assertThat(response.pageInfo().getTotalElements()).isEqualTo(page.getTotalElements());
            }
        }
    }

    @Nested
    @DisplayName("채팅방_나가기")
    class exitRoom {

        @Nested
        class 성공 {

            @Test
            void 정상적인_방_나가기_시_참여자_상태_비활성화() {
                // given
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest(
                        "room123",
                        1L
                );

                DirectMessageRoom room = DirectMessageRoom.builder()
                        .id("room123")
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                        ))
                        .build();

                given(directMessageRoomRepository.findById("room123")).willReturn(Optional.of(room));
                given(directMessageRoomRepository.save(any(DirectMessageRoom.class))).willReturn(room);

                // when
                directMessageService.exitRoom(request);

                // then
                verify(directMessageRoomRepository).findById("room123");
                verify(directMessageRoomRepository).save(any(DirectMessageRoom.class));
                verify(directMessageRoomRepository, never()).delete(any(DirectMessageRoom.class));
            }
        }

        @Nested
        class 실패 {

            @Test
            void 존재하지_않는_채팅방에서_나가기_시_예외_발생() {
                // given
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest(
                        "nonexistent",
                        1L
                );

                given(directMessageRoomRepository.findById(request.roomId())).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> directMessageService.exitRoom(request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);

                verify(directMessageRoomRepository).findById(request.roomId());
                verify(directMessageRoomRepository, never()).save(any());
            }

            @Test
            void 참여하지_않은_사용자가_방_나가기_시_예외_발생() {
                // given
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest(
                        "room123",
                        999L
                );

                DirectMessageRoom room = DirectMessageRoom.builder()
                        .id("room123")
                        .participants(List.of(
                                Participant.builder().userId(1L).nickname("user1").isActive(true).build(),
                                Participant.builder().userId(2L).nickname("user2").isActive(true).build()
                        ))
                        .build();

                given(directMessageRoomRepository.findById(request.roomId())).willReturn(Optional.of(room));

                // when & then
                assertThatThrownBy(() -> directMessageService.exitRoom(request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.USER_NOT_IN_ROOM);

                verify(directMessageRoomRepository).findById(request.roomId());
                verify(directMessageRoomRepository, never()).save(any());
            }
        }
    }
}
