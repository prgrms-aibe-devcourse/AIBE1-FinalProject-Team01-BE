package kr.co.amateurs.server.fixture.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.fixture.common.TestConstants;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRoomRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DirectMessageRoomFixture {

    // Room ID 상수들
    public static final String ROOM_1 = "room1";
    public static final String ROOM_2 = "room2";
    public static final String ROOM_3 = "room3";
    public static final String EXISTING_ROOM = "existing_room";
    public static final String EMPTY_ROOM = "empty_room";

    private final DirectMessageRoomRepository repository;

    public DirectMessageRoomFixture(DirectMessageRoomRepository repository) {
        this.repository = repository;
    }

    public DirectMessageRoom createAndSaveRoom(String roomId) {
        return createAndSaveRoom(
                roomId,
                TestConstants.USER_ID_1,
                TestConstants.USER_NAME_1,
                TestConstants.USER_ID_2,
                TestConstants.USER_NAME_2
        );
    }

    public DirectMessageRoom createAndSaveRoom(String roomId, String lastMessage) {
        DirectMessageRoom room = createAndSaveRoom(roomId);
        room.updateLastMessage(lastMessage);
        return repository.save(room);
    }

    public DirectMessageRoom createAndSaveRoom(
            String roomId,
            Long user1Id,
            String user1Name,
            Long user2Id,
            String user2Name
    ) {
        DirectMessageRoom room = DirectMessageRoom.builder()
                .id(roomId)
                .participants(List.of(
                        createParticipant(user1Id, user1Name),
                        createParticipant(user2Id, user2Name)
                ))
                .build();
        return repository.save(room);
    }

    public DirectMessageRoom createAndSaveInactiveRoom(String roomId) {
        DirectMessageRoom room = createAndSaveRoom(roomId);
        room.getParticipants().get(0).exitRoom(); // 첫 번째 참여자 비활성화
        return repository.save(room);
    }

    // 다중 방 생성
    public void createMultipleRoomsWithMessages() {
        createAndSaveRoom(ROOM_1, DirectMessageFixture.LAST_MESSAGE_1);
        DirectMessageRoom room2 = createAndSaveRoom(
                ROOM_2,
                TestConstants.USER_ID_1,
                TestConstants.USER_NAME_1,
                TestConstants.USER_ID_3,
                TestConstants.USER_NAME_3);
        room2.updateLastMessage(DirectMessageFixture.LAST_MESSAGE_2);
        repository.save(room2);
    }

    private Participant createParticipant(Long userId, String nickname) {
        return Participant.builder()
                .userId(userId)
                .nickname(nickname)
                .isActive(true)
                .build();
    }
}