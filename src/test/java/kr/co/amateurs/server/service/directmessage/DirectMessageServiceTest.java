package kr.co.amateurs.server.service.directmessage;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.dto.directmessage.event.AnonymizeEvent;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.common.TestConstants;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.fixture.directmessage.DirectMessageFixture;
import kr.co.amateurs.server.fixture.directmessage.DirectMessageRoomFixture;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRoomRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.alarm.SseService;
import kr.co.amateurs.server.service.file.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("test")
@Transactional
class DirectMessageServiceTest {

    @Autowired
    private DirectMessageService directMessageService;

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private DirectMessageRoomRepository directMessageRoomRepository;

    @Autowired
    private DirectMessageFixture messageFixture;

    @Autowired
    private DirectMessageRoomFixture roomFixture;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private SseService sseService;

    private User testUser1;
    private User testUser2;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        directMessageRepository.deleteAll();
        directMessageRoomRepository.deleteAll();

        // UserTestFixture를 활용한 테스트 사용자 생성
        testUser1 = createTestUserWithId(TestConstants.USER_ID_1, TestConstants.USER_NAME_1, "user1@test.com");
        testUser2 = createTestUserWithId(TestConstants.USER_ID_2, TestConstants.USER_NAME_2, "user2@test.com");
        deletedUser = createTestUserWithId(TestConstants.USER_ID_3, "알수없음", "deleted@test.com");

        // UserService Mock 설정
        when(userService.getCurrentLoginUser()).thenReturn(testUser1);
        when(userService.findById(TestConstants.USER_ID_1)).thenReturn(testUser1);
        when(userService.findById(TestConstants.USER_ID_2)).thenReturn(testUser2);
        when(userService.findById(TestConstants.USER_ID_3)).thenReturn(deletedUser);
    }

    private User createTestUserWithId(Long id, String nickname, String email) {
        User user = UserTestFixture.defaultUser()
                .email(email)
                .nickname(nickname)
                .name(nickname)
                .build();

        // ReflectionTestUtils를 사용하여 id 필드 설정
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    class SaveMessage {

        @Test
        void 정상적인_메시지_저장_시_DirectMessageResponse를_반환한다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            DirectMessageRequest request = DirectMessageRequest.builder()
                    .content(DirectMessageFixture.MESSAGE_CONTENT_1)
                    .senderId(TestConstants.USER_ID_1)
                    .senderName(TestConstants.USER_NAME_1)
                    .messageType(MessageType.TEXT)
                    .build();

            // when
            DirectMessageResponse response = directMessageService.saveMessage(room.getId(), request);

            // then
            assertThat(response.content()).isEqualTo(DirectMessageFixture.MESSAGE_CONTENT_1);
            assertThat(response.senderId()).isEqualTo(TestConstants.USER_ID_1);
            assertThat(response.senderNickname()).isEqualTo(TestConstants.USER_NAME_1);
            assertThat(response.roomId()).isEqualTo(room.getId());
            assertThat(response.messageType()).isEqualTo(MessageType.TEXT);
        }

        @Test
        void 존재하지_않는_채팅방에_메시지_저장_시_NOT_FOUND_ROOM_예외가_발생한다() {
            // given
            DirectMessageRequest request = DirectMessageRequest.builder()
                    .content(DirectMessageFixture.MESSAGE_CONTENT_1)
                    .senderId(TestConstants.USER_ID_1)
                    .senderName(TestConstants.USER_NAME_1)
                    .messageType(MessageType.TEXT)
                    .build();

            // when & then
            assertThatThrownBy(() -> directMessageService.saveMessage("nonexistent", request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_FOUND_ROOM);
        }
    }

    @Nested
    class CreateRoom {

        @Test
        void 새로운_채팅방_생성_시_DirectMessageRoomResponse를_반환한다() {
            // when
            DirectMessageRoomResponse response = directMessageService.createRoom(TestConstants.USER_ID_2);

            // then
            assertThat(response.id()).isNotNull();
            assertThat(response.partnerId()).isEqualTo(TestConstants.USER_ID_2);
            assertThat(directMessageRoomRepository.findById(response.id())).isPresent();
        }

        @Test
        void 자신과_채팅방_생성_시_CANNOT_CHAT_WITH_SELF_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> directMessageService.createRoom(TestConstants.USER_ID_1))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }
    }

    @Nested
    class GetRooms {

        @Test
        void 사용자의_활성_채팅방_목록을_반환한다() {
            // given - Fixture를 활용한 다중 채팅방 생성
            roomFixture.createMultipleRoomsWithMessages();

            // when
            List<DirectMessageRoomResponse> responses = directMessageService.getRooms();

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses).extracting("lastMessage")
                    .containsExactlyInAnyOrder(
                            DirectMessageFixture.LAST_MESSAGE_1,
                            DirectMessageFixture.LAST_MESSAGE_2
                    );
        }
    }

    @Nested
    class GetMessages {

        @Test
        void 채팅방의_메시지_목록을_페이지네이션으로_반환한다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createMultipleMessages(room.getId(), 5);

            DirectMessagePaginationParam param = DirectMessagePaginationParam.builder()
                    .roomId(room.getId())
                    .userId(TestConstants.USER_ID_1)
                    .page(0)
                    .size(10)
                    .build();

            // when
            DirectMessagePageResponse response = directMessageService.getMessages(param);

            // then
            assertThat(response.messages()).hasSize(5);
        }

        @Test
        void 사용자가_나갔다가_다시_들어온_후_재입장_시점_이후_메시지만_조회된다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createMessagesBeforeAndAfterUserLeft(room.getId());

            // 사용자가 방을 나간 후
            directMessageService.exitRoom(room.getId());

            // 사용자가 다시 방에 들어옴 (createRoom 호출하면 reEntry 처리됨)
            directMessageService.createRoom(TestConstants.USER_ID_2);

            DirectMessagePaginationParam param = DirectMessagePaginationParam.builder()
                    .roomId(room.getId())
                    .userId(TestConstants.USER_ID_1)
                    .page(0)
                    .size(10)
                    .build();

            // when
            DirectMessagePageResponse response = directMessageService.getMessages(param);

            // then
            // 재입장 시점 이후 메시지만 조회되어야 함 (나가기 전 메시지는 안 보임)
            assertThat(response.messages()).hasSize(1);
            assertThat(response.messages().get(0).content()).isEqualTo(DirectMessageFixture.MESSAGE_CONTENT_AFTER_LEFT);
        }
    }

    @Nested
    class ExitRoom {

        @Test
        void 정상적인_방_나가기_시_참여자_상태가_비활성화된다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);

            // when
            directMessageService.exitRoom(room.getId());

            // then
            DirectMessageRoom updatedRoom = directMessageRoomRepository.findById(room.getId()).orElseThrow();
            assertThat(updatedRoom.getParticipants().stream()
                    .anyMatch(p -> p.getUserId().equals(TestConstants.USER_ID_1) && !p.getIsActive()))
                    .isTrue();
        }

        @Test
        void 모든_참여자가_나간_방은_삭제된다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createTestMessages(room.getId());

            // 첫 번째 사용자가 방을 나감
            directMessageService.exitRoom(room.getId());

            // Mock 설정 변경 - 두 번째 사용자로 전환
            when(userService.getCurrentLoginUser()).thenReturn(testUser2);

            // when - 두 번째 사용자도 방을 나감
            directMessageService.exitRoom(room.getId());

            // then
            assertThat(directMessageRoomRepository.findById(room.getId())).isEmpty();
            assertThat(directMessageRepository.findAll()).isEmpty();
        }
    }

    @Nested
    class AnonymizeUser {

        @Test
        void 사용자_익명화_이벤트_발생_시_해당_사용자의_모든_메시지가_익명화된다() throws InterruptedException {
            // given
            DirectMessageRoom room1 = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            DirectMessageRoom room2 = roomFixture.createAndSaveRoom(
                    DirectMessageRoomFixture.ROOM_2,
                    TestConstants.USER_ID_2,
                    TestConstants.USER_NAME_2,
                    TestConstants.USER_ID_3,
                    TestConstants.USER_NAME_3
            );

            // 여러 방에 사용자2의 메시지 생성
            messageFixture.createAndSaveMessage(room1.getId(), "첫 번째 방 메시지", TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
            messageFixture.createAndSaveMessage(room1.getId(), "사용자1 메시지", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
            messageFixture.createAndSaveMessage(room2.getId(), "두 번째 방 메시지", TestConstants.USER_ID_2, TestConstants.USER_NAME_2);

            // 익명화할 사용자 생성
            User userToAnonymize = createTestUserWithId(TestConstants.USER_ID_2, "알수없음", "anonymous@test.com");
            AnonymizeEvent event = new AnonymizeEvent(userToAnonymize);

            // when
            directMessageService.anonymizeUser(event);

            Thread.sleep(1000);

            // then
            List<DirectMessage> allMessages = directMessageRepository.findAll();

            // 사용자2의 메시지들이 익명화되었는지 확인
            List<DirectMessage> user2Messages = allMessages.stream()
                    .filter(msg -> msg.getSenderId().equals(TestConstants.USER_ID_2))
                    .toList();

            assertThat(user2Messages).hasSize(2)
                    .allSatisfy(msg -> {
                        assertThat(msg.getSenderNickname()).isEqualTo("알수없음");
                        assertThat(msg.getSenderProfileImage()).isEqualTo(userToAnonymize.getImageUrl());
                    });

            // 다른 사용자의 메시지는 변경되지 않았는지 확인
            List<DirectMessage> user1Messages = allMessages.stream()
                    .filter(msg -> msg.getSenderId().equals(TestConstants.USER_ID_1))
                    .toList();

            assertThat(user1Messages).hasSize(1);
            assertThat(user1Messages.get(0).getSenderNickname()).isEqualTo(TestConstants.USER_NAME_1);
        }

        @Test
        void 이미_익명화된_사용자의_재익명화_요청_시_정상적으로_처리된다() throws InterruptedException {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createAndSaveMessage(room.getId(), "테스트 메시지", TestConstants.USER_ID_2, "이미익명화된닉네임");

            User alreadyAnonymizedUser = createTestUserWithId(TestConstants.USER_ID_2, "알수없음", "anonymous@test.com");
            AnonymizeEvent event = new AnonymizeEvent(alreadyAnonymizedUser);

            // when
            directMessageService.anonymizeUser(event);

            Thread.sleep(1000);

            // then
            List<DirectMessage> messages = directMessageRepository.findAll();
            assertThat(messages).hasSize(1);
            assertThat(messages.get(0).getSenderNickname()).isEqualTo("알수없음");
        }

        @Test
        void 여러_사용자_동시_익명화_시_각각_올바르게_처리된다() throws InterruptedException {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createAndSaveMessage(room.getId(), "사용자1 메시지", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
            messageFixture.createAndSaveMessage(room.getId(), "사용자2 메시지", TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
            messageFixture.createAndSaveMessage(room.getId(), "사용자3 메시지", TestConstants.USER_ID_3, TestConstants.USER_NAME_3);

            User anonymizedUser2 = createTestUserWithId(TestConstants.USER_ID_2, "알수없음", "anon2@test.com");
            User anonymizedUser3 = createTestUserWithId(TestConstants.USER_ID_3, "알수없음", "anon3@test.com");

            // when - 두 사용자 순차적으로 익명화
            directMessageService.anonymizeUser(new AnonymizeEvent(anonymizedUser2));
            directMessageService.anonymizeUser(new AnonymizeEvent(anonymizedUser3));

            Thread.sleep(1000);

            // then
            List<DirectMessage> allMessages = directMessageRepository.findAll();
            assertThat(allMessages).hasSize(3);

            // 익명화된 사용자들의 메시지 확인
            List<DirectMessage> anonymizedMessages = allMessages.stream()
                    .filter(msg -> msg.getSenderId().equals(TestConstants.USER_ID_2) ||
                            msg.getSenderId().equals(TestConstants.USER_ID_3))
                    .toList();

            assertThat(anonymizedMessages).hasSize(2)
                    .allSatisfy(msg ->
                            assertThat(msg.getSenderNickname()).isEqualTo("알수없음")
                    );

            // 익명화되지 않은 사용자 메시지 확인
            DirectMessage user1Message = allMessages.stream()
                    .filter(msg -> msg.getSenderId().equals(TestConstants.USER_ID_1))
                    .findFirst()
                    .orElseThrow();

            assertThat(user1Message.getSenderNickname()).isEqualTo(TestConstants.USER_NAME_1);
        }
    }

    @Nested
    class FindMessages {

        @Test
        void 키워드로_메시지_검색_시_관련_메시지들이_반환된다() {
            // given
            DirectMessageRoom room1 = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            DirectMessageRoom room2 = roomFixture.createAndSaveRoom(
                    DirectMessageRoomFixture.ROOM_2,
                    TestConstants.USER_ID_1,
                    TestConstants.USER_NAME_1,
                    TestConstants.USER_ID_3,
                    TestConstants.USER_NAME_3
            );

            // 픽스처를 활용한 검색 테스트 메시지 생성
            messageFixture.createAndSaveMessage(room1.getId(), DirectMessageFixture.SEARCH_CONTENT_1, TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
            messageFixture.createAndSaveMessage(room1.getId(), "오늘 날씨가 좋네요", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
            messageFixture.createAndSaveMessage(room2.getId(), DirectMessageFixture.SEARCH_CONTENT_2, TestConstants.USER_ID_3, TestConstants.USER_NAME_3);
            messageFixture.createAndSaveMessage(room2.getId(), "내일 봐요", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam(DirectMessageFixture.SEARCH_KEYWORD_HELLO);

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(response.content())
                    .extracting(DirectMessageResponse::content)
                    .containsExactlyInAnyOrder(DirectMessageFixture.SEARCH_CONTENT_1, DirectMessageFixture.SEARCH_CONTENT_2);
        }

        @Test
        void 참여하지_않은_방의_메시지는_검색되지_않는다() {
            // given
            DirectMessageRoom room1 = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1); // user1, user2
            DirectMessageRoom room2 = roomFixture.createAndSaveRoom(
                    DirectMessageRoomFixture.ROOM_2,
                    TestConstants.USER_ID_2,
                    TestConstants.USER_NAME_2,
                    TestConstants.USER_ID_3,
                    TestConstants.USER_NAME_3
            ); // user2, user3 (user1은 참여 안함)

            messageFixture.createAndSaveMessage(room1.getId(), "참여한 방 테스트", TestConstants.USER_ID_2, TestConstants.USER_NAME_2);
            messageFixture.createAndSaveMessage(room2.getId(), "참여하지 않은 방 테스트", TestConstants.USER_ID_3, TestConstants.USER_NAME_3);

            DirectMessageSearchPaginationParam param = DirectMessageSearchPaginationParam.builder()
                    .keyword("테스트")
                    .page(0)
                    .size(10)
                    .build();

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).content()).isEqualTo("참여한 방 테스트");
        }

        @Test
        void 재입장한_방에서는_재입장_이후_메시지만_검색된다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);

            // 재입장 전 메시지
            messageFixture.createAndSaveMessage(room.getId(), DirectMessageFixture.SEARCH_REENTRY_BEFORE, TestConstants.USER_ID_2, TestConstants.USER_NAME_2);

            // 사용자가 방을 나감
            directMessageService.exitRoom(room.getId());

            // 사용자가 다시 방에 들어옴
            directMessageService.createRoom(TestConstants.USER_ID_2);

            // 재입장 후 메시지
            messageFixture.createAndSaveMessage(room.getId(), DirectMessageFixture.SEARCH_REENTRY_AFTER, TestConstants.USER_ID_2, TestConstants.USER_NAME_2);

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam("검색 테스트");

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).content()).isEqualTo(DirectMessageFixture.SEARCH_REENTRY_AFTER);
        }

        @Test
        void 검색_결과가_최신순으로_정렬된다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);

            // 픽스처를 활용한 시간 순서 테스트 메시지 생성
            messageFixture.createTimeSortedMessages(room.getId());

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam("테스트");

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(response.content().get(0).content()).isEqualTo("두 번째 테스트"); // 최신순 첫 번째
            assertThat(response.content().get(1).content()).isEqualTo("첫 번째 테스트"); // 최신순 두 번째
        }

        @Test
        void 검색어가_없는_경우_모든_메시지가_검색된다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createAndSaveMessage(room.getId(), "첫 번째 메시지", TestConstants.USER_ID_1, TestConstants.USER_NAME_1);
            messageFixture.createAndSaveMessage(room.getId(), "두 번째 메시지", TestConstants.USER_ID_2, TestConstants.USER_NAME_2);

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam("");

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).hasSize(2);
        }

        @Test
        void MongoDB_정규표현식_검색이_올바르게_동작한다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createEnglishSearchTestMessages(room.getId());

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam("hello");

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then - 대소문자 구분 없이 모든 hello 메시지가 검색되어야 함
            assertThat(response.content()).hasSize(3);
            assertThat(response.content())
                    .extracting(DirectMessageResponse::content)
                    .containsExactlyInAnyOrder(
                            DirectMessageFixture.SEARCH_CONTENT_EN_1,
                            DirectMessageFixture.SEARCH_CONTENT_EN_2,
                            DirectMessageFixture.SEARCH_CONTENT_EN_3
                    );
        }

        @Test
        void 부분_문자열_검색이_동작한다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            messageFixture.createSearchTestMessages(room.getId());

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam(DirectMessageFixture.SEARCH_KEYWORD_HELLO);

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(response.content())
                    .extracting(DirectMessageResponse::content)
                    .containsExactlyInAnyOrder(DirectMessageFixture.SEARCH_CONTENT_1, DirectMessageFixture.SEARCH_CONTENT_2);
        }

        @Test
        void 복합_조건_검색이_올바르게_동작한다() {
            // given
            DirectMessageRoom room1 = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);
            DirectMessageRoom room2 = roomFixture.createAndSaveRoom(
                    DirectMessageRoomFixture.ROOM_2,
                    TestConstants.USER_ID_1,
                    TestConstants.USER_NAME_1,
                    TestConstants.USER_ID_3,
                    TestConstants.USER_NAME_3
            );

            // 픽스처를 활용한 다중 방 검색 테스트 메시지 생성
            messageFixture.createMultipleRoomSearchMessages(room1.getId(), room2.getId());

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam("테스트");

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then - 두 방 모두에서 "테스트"가 포함된 메시지가 검색되어야 함
            assertThat(response.content()).hasSize(2);
            assertThat(response.content())
                    .extracting(DirectMessageResponse::content)
                    .containsExactlyInAnyOrder(DirectMessageFixture.SEARCH_CONTENT_MIXED + " 1", DirectMessageFixture.SEARCH_CONTENT_MIXED + " 2");

            // 각 메시지가 올바른 방에서 온 것인지 확인
            assertThat(response.content())
                    .extracting(DirectMessageResponse::roomId)
                    .containsExactlyInAnyOrder(room1.getId(), room2.getId());
        }

        @Test
        void 페이징이_올바르게_동작한다() {
            // given
            DirectMessageRoom room = roomFixture.createAndSaveRoom(DirectMessageRoomFixture.ROOM_1);

            // 픽스처를 활용한 페이징 테스트 메시지 생성
            messageFixture.createPaginationTestMessages(room.getId(), 5);

            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam("페이징", 0, 3);

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).hasSize(3);
            assertThat(response.pageInfo().getTotalElements()).isEqualTo(5);
            assertThat(response.pageInfo().getTotalPages()).isEqualTo(2);
        }

        @Test
        void 참여한_방이_없으면_빈_결과를_반환한다() {
            // given - 현재 사용자가 참여한 방이 없음
            DirectMessageSearchPaginationParam param = DirectMessageFixture.createSearchPaginationParam("테스트");

            // when
            PageResponseDTO<DirectMessageResponse> response = directMessageService.findMessages(param);

            // then
            assertThat(response.content()).isEmpty();
            assertThat(response.pageInfo().getTotalElements()).isZero();
        }
    }
}
