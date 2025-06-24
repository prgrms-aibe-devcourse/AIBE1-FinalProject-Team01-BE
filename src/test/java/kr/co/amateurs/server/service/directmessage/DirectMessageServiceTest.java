package kr.co.amateurs.server.service.directmessage;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.common.TestConstants;
import kr.co.amateurs.server.fixture.directmessage.*;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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

    // Fixtures
    private DirectMessageRoomFixture roomFixture;
    private DirectMessageFixture messageFixture;

    @BeforeEach
    void setUp() {
        directMessageRepository.deleteAll();
        directMessageRoomRepository.deleteAll();
        
        // Initialize fixtures
        roomFixture = new DirectMessageRoomFixture(directMessageRoomRepository);
        messageFixture = new DirectMessageFixture(directMessageRepository);
    }

    @Nested
    class SaveMessage {

        @Nested
        class 성공 {

            @Test
            void 정상적인_메시지_저장_시_DirectMessageResponse를_반환한다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
                DirectMessageRequest request = DirectMessageRequestFixture.createBasicRequest();

                // when
                DirectMessageResponse response = directMessageService.saveMessage(DirectMessageRoomFixture.ROOM_1, request);

                // then
                assertThat(response.content()).isEqualTo(DirectMessageFixture.MESSAGE_CONTENT_1);
                assertThat(response.senderId()).isEqualTo(TestConstants.USER_ID_1);
                assertThat(response.senderNickname()).isEqualTo(TestConstants.USER_NAME_1);
                assertThat(response.roomId()).isEqualTo(DirectMessageRoomFixture.ROOM_1);
                assertThat(response.messageType()).isEqualTo(MessageType.TEXT);

                assertThat(directMessageRepository.findById(response.id()))
                        .isPresent()
                        .hasValueSatisfying(m -> {
                            assertThat(m.getContent()).isEqualTo(response.content());
                        });
            }

            @Test
            void 메시지_저장_시_채팅방의_마지막_메시지가_업데이트된다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_2, DirectMessageFixture.MESSAGE_CONTENT_OLD);
                DirectMessageRequest request = DirectMessageRequestFixture.createNewMessageRequest();

                // when
                directMessageService.saveMessage(DirectMessageRoomFixture.ROOM_2, request);

                // then
                assertThat(directMessageRoomRepository.findById(DirectMessageRoomFixture.ROOM_2))
                        .isPresent()
                        .hasValueSatisfying(room -> {
                            assertThat(room.getLastMessage()).isEqualTo(DirectMessageFixture.MESSAGE_CONTENT_2);
                        });
            }
        }

        @Nested
        class 실패 {

            @Test
            void 존재하지_않는_채팅방에_메시지_저장_시_NOT_FOUND_ROOM_예외가_발생한다() {
                // given
                DirectMessageRequest request = DirectMessageRequestFixture.createBasicRequest();

                // when & then
                assertThatThrownBy(() -> directMessageService.saveMessage(TestConstants.NONEXISTENT_ID, request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);

                assertThat(directMessageRepository.findAll()).isEmpty();
            }

            @Test
            void 채팅방에_참여하지_않은_사용자가_메시지_저장_시_USER_NOT_IN_ROOM_예외가_발생한다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_3);
                DirectMessageRequest request = DirectMessageRequestFixture.createUnauthorizedRequest();

                // when & then
                assertThatThrownBy(() -> directMessageService.saveMessage(DirectMessageRoomFixture.ROOM_3, request))
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
                DirectMessageRoomCreateRequest request = DirectMessageRequestFixture.createRoomRequest();

                // when
                DirectMessageRoomResponse response = directMessageService.createRoom(request);

                // then
                assertThat(response.roomId()).isNotNull();
                assertThat(response.otherUserId()).isEqualTo(TestConstants.USER_ID_2);

                assertThat(directMessageRoomRepository.findById(response.roomId()))
                        .isPresent()
                        .hasValueSatisfying(room -> {
                            assertThat(room.getParticipants()).hasSize(2);
                            assertThat(room.getId()).isEqualTo(response.roomId());
                        });
            }

            @Test
            void 기존_채팅방이_있는_경우_기존_방_정보를_반환한다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.EXISTING_ROOM, "기존 메시지");
                DirectMessageRoomCreateRequest request = DirectMessageRequestFixture.createRoomRequest();

                // when
                DirectMessageRoomResponse response = directMessageService.createRoom(request);

                // then
                assertThat(response.roomId()).isEqualTo(DirectMessageRoomFixture.EXISTING_ROOM);
                assertThat(response.lastMessage()).isEqualTo("기존 메시지");
                assertThat(response.otherUserId()).isEqualTo(TestConstants.USER_ID_2);

                // 중복 생성되지 않았는지 확인
                assertThat(directMessageRoomRepository.findAll()).hasSize(1);
            }

            @Test
            void 비활성화된_참여자가_있는_기존_방의_경우_재입장_처리를_한다() {
                // given
                roomFixture.createAndSaveInactiveRoom(DirectMessageRoomFixture.EXISTING_ROOM);
                DirectMessageRoomCreateRequest request = DirectMessageRequestFixture.createRoomRequest();

                // when
                directMessageService.createRoom(request);

                // then
                assertThat(directMessageRoomRepository.findById(DirectMessageRoomFixture.EXISTING_ROOM))
                        .isPresent()
                        .hasValueSatisfying(room -> {
                            Participant user1 = room.getParticipants().stream()
                                    .filter(p -> p.getUserId().equals(TestConstants.USER_ID_1))
                                    .findFirst()
                                    .orElseThrow();
                            assertThat(user1.getIsActive()).isTrue();
                        });
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
                roomFixture.createMultipleRoomsWithMessages();

                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(TestConstants.USER_ID_1);

                // then
                assertThat(responses).hasSize(2);
                assertThat(responses.get(0).roomId()).isEqualTo(DirectMessageRoomFixture.ROOM_1);
                assertThat(responses.get(0).otherUserId()).isEqualTo(TestConstants.USER_ID_2);
                assertThat(responses.get(0).lastMessage()).isEqualTo(DirectMessageFixture.LAST_MESSAGE_1);

                assertThat(responses.get(1).roomId()).isEqualTo(DirectMessageRoomFixture.ROOM_2);
                assertThat(responses.get(1).otherUserId()).isEqualTo(TestConstants.USER_ID_3);
                assertThat(responses.get(1).lastMessage()).isEqualTo(DirectMessageFixture.LAST_MESSAGE_2);
            }

            @Test
            void 채팅방이_없는_사용자의_경우_빈_목록을_반환한다() {
                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(TestConstants.NONEXISTENT_USER_ID);

                // then
                assertThat(responses).isEmpty();
            }

            @Test
            void 비활성화된_참여자는_채팅방_목록에서_제외된다() {
                // given
                roomFixture.createAndSaveInactiveRoom(DirectMessageRoomFixture.ROOM_1);

                // when
                List<DirectMessageRoomResponse> responses = directMessageService.getRooms(TestConstants.USER_ID_1);

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
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
                messageFixture.createMultipleMessages(DirectMessageRoomFixture.ROOM_1, 15);
                DirectMessagePaginationParam param = DirectMessageRequestFixture.createPaginationParam(DirectMessageRoomFixture.ROOM_1, TestConstants.USER_ID_1);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.messages()).hasSize(10);
                assertThat(response.pageInfo().getTotalElements()).isEqualTo(15);
                assertThat(response.pageInfo().getTotalPages()).isEqualTo(2);
                assertThat(response.pageInfo().getPageNumber()).isZero();

                assertThat(response.messages().get(0).content()).isEqualTo("메시지 15");
                assertThat(response.messages().get(9).content()).isEqualTo("메시지 6");
            }

            @Test
            void 사용자가_나간_시점_이후의_메시지만_조회한다() {
                // given
                roomFixture.createAndSaveInactiveRoom(DirectMessageRoomFixture.ROOM_1);
                messageFixture.createMessagesBeforeAndAfterUserLeft(DirectMessageRoomFixture.ROOM_1);
                DirectMessagePaginationParam param = DirectMessageRequestFixture.createPaginationParam(DirectMessageRoomFixture.ROOM_1, TestConstants.USER_ID_1);

                // when
                DirectMessagePageResponse response = directMessageService.getMessages(param);

                // then
                assertThat(response.messages()).hasSize(1);
                assertThat(response.messages().get(0).content()).isEqualTo(DirectMessageFixture.MESSAGE_CONTENT_AFTER_LEFT);
            }

            @Test
            void 메시지가_없는_채팅방의_경우_빈_페이지를_반환한다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.EMPTY_ROOM);
                DirectMessagePaginationParam param = DirectMessageRequestFixture.createPaginationParam(DirectMessageRoomFixture.EMPTY_ROOM, TestConstants.USER_ID_1);

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
                DirectMessagePaginationParam param = DirectMessageRequestFixture.createPaginationParam(TestConstants.NONEXISTENT_ID, TestConstants.USER_ID_1);

                // when & then
                assertThatThrownBy(() -> directMessageService.getMessages(param))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);
            }

            @Test
            void 채팅방에_참여하지_않은_사용자가_메시지_조회_시_USER_NOT_IN_ROOM_예외가_발생한다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
                DirectMessagePaginationParam param = DirectMessageRequestFixture.createPaginationParam(DirectMessageRoomFixture.ROOM_1, TestConstants.USER_ID_3);

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
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
                DirectMessageRoomExitRequest request = DirectMessageRequestFixture.createExitRequest(DirectMessageRoomFixture.ROOM_1, TestConstants.USER_ID_1);

                // when
                directMessageService.exitRoom(request);

                // then
                assertThat(directMessageRoomRepository.findById(DirectMessageRoomFixture.ROOM_1))
                        .isPresent()
                        .hasValueSatisfying(room -> {
                            Participant user1 = room.getParticipants().stream()
                                    .filter(p -> p.getUserId().equals(TestConstants.USER_ID_1))
                                    .findFirst()
                                    .orElseThrow();
                            assertThat(user1.getIsActive()).isFalse();
                            assertThat(user1.getLeftAt()).isNotNull();

                            Participant user2 = room.getParticipants().stream()
                                    .filter(p -> p.getUserId().equals(TestConstants.USER_ID_2))
                                    .findFirst()
                                    .orElseThrow();
                            assertThat(user2.getIsActive()).isTrue();
                        });
            }

            @Test
            void 모든_참여자가_나간_경우_채팅방과_메시지가_삭제된다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_2);
                messageFixture.createTestMessages(DirectMessageRoomFixture.ROOM_2);

                // when - 두 사용자 모두 나가기
                directMessageService.exitRoom(DirectMessageRequestFixture.createExitRequest(DirectMessageRoomFixture.ROOM_2, TestConstants.USER_ID_1));
                directMessageService.exitRoom(DirectMessageRequestFixture.createExitRequest(DirectMessageRoomFixture.ROOM_2, TestConstants.USER_ID_2));

                // then
                assertThat(directMessageRoomRepository.findById(DirectMessageRoomFixture.ROOM_2)).isEmpty();
                assertThat(directMessageRepository.findAll()).isEmpty();
            }

            @Test
            void 한_명이_나간_후_다시_참여할_수_있다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_3);
                DirectMessageRoomExitRequest exitRequest = DirectMessageRequestFixture.createExitRequest(DirectMessageRoomFixture.ROOM_3, TestConstants.USER_ID_1);

                // when - user1이 나감
                directMessageService.exitRoom(exitRequest);

                // then - user1이 비활성화됨
                assertThat(directMessageRoomRepository.findById(DirectMessageRoomFixture.ROOM_3))
                        .isPresent()
                        .hasValueSatisfying(room -> {
                            Participant user1 = room.getParticipants().stream()
                                    .filter(p -> p.getUserId().equals(TestConstants.USER_ID_1))
                                    .findFirst()
                                    .orElseThrow();
                            assertThat(user1.getIsActive()).isFalse();
                        });

                // when - user1이 다시 참여
                DirectMessageRoomCreateRequest createRequest = DirectMessageRequestFixture.createRoomRequest();
                directMessageService.createRoom(createRequest);

                // then - user1이 다시 활성화됨
                assertThat(directMessageRoomRepository.findById(DirectMessageRoomFixture.ROOM_3))
                        .isPresent()
                        .hasValueSatisfying(room -> {
                            Participant reJoinedUser1 = room.getParticipants().stream()
                                    .filter(p -> p.getUserId().equals(TestConstants.USER_ID_1))
                                    .findFirst()
                                    .orElseThrow();
                            assertThat(reJoinedUser1.getIsActive()).isTrue();
                        });
            }
        }

        @Nested
        class 실패 {

            @Test
            void 존재하지_않는_채팅방에서_나가기_시_NOT_FOUND_ROOM_예외가_발생한다() {
                // given
                DirectMessageRoomExitRequest request = DirectMessageRequestFixture.createExitRequest(TestConstants.NONEXISTENT_ID, TestConstants.USER_ID_1);

                // when & then
                assertThatThrownBy(() -> directMessageService.exitRoom(request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.NOT_FOUND_ROOM);
            }

            @Test
            void 참여하지_않은_사용자가_방_나가기_시_USER_NOT_IN_ROOM_예외가_발생한다() {
                // given
                roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_3);
                DirectMessageRoomExitRequest request = DirectMessageRequestFixture.createExitRequest(DirectMessageRoomFixture.ROOM_3, TestConstants.NONEXISTENT_USER_ID);

                // when & then
                assertThatThrownBy(() -> directMessageService.exitRoom(request))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(ErrorCode.USER_NOT_IN_ROOM);
            }
        }
    }
}