package kr.co.amateurs.server.fixture.directmessage;

import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import org.springframework.data.domain.Sort;

import java.util.Map;

public class DirectMessageRequestFixture {
    
    // 메시지 요청 생성
    public static DirectMessageRequest createBasicRequest() {
        return createMessageRequest(DirectMessageFixture.MESSAGE_CONTENT_1, 1L, "user1");
    }
    
    public static DirectMessageRequest createNewMessageRequest() {
        return createMessageRequest(DirectMessageFixture.MESSAGE_CONTENT_2, 1L, "user1");
    }
    
    public static DirectMessageRequest createUnauthorizedRequest() {
        return createMessageRequest(DirectMessageFixture.UNAUTHORIZED_MESSAGE, 3L, "user3");
    }
    
    // 방 생성 요청
    public static DirectMessageRoomCreateRequest createRoomRequest() {
        return DirectMessageRoomCreateRequest.builder()
                .participantMap(Map.of(1L, "user1", 2L, "user2"))
                .build();
    }
    
    // 페이지네이션 요청 - 오버로딩으로 중복 제거
    public static DirectMessagePaginationParam createPaginationParam(String roomId, Long userId) {
        return createPaginationParam(roomId, userId, 0, 10);
    }
    
    public static DirectMessagePaginationParam createPaginationParam(String roomId, Long userId, int page, int size) {
        return DirectMessagePaginationParam.builder()
                .roomId(roomId)
                .userId(userId)
                .page(page)
                .size(size)
                .field(PaginationSortType.DM_SENT_AT)
                .sortDirection(Sort.Direction.DESC)
                .build();
    }
    
    // 방 나가기 요청
    public static DirectMessageRoomExitRequest createExitRequest(String roomId, Long userId) {
        return new DirectMessageRoomExitRequest(roomId, userId);
    }
    
    // 공통 메시지 요청 생성 메서드
    private static DirectMessageRequest createMessageRequest(String content, Long senderId, String senderName) {
        return DirectMessageRequest.builder()
                .content(content)
                .senderId(senderId)
                .senderName(senderName)
                .messageType(MessageType.TEXT)
                .build();
    }
}