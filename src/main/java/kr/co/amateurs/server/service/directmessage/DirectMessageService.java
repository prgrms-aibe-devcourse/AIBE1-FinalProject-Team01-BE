package kr.co.amateurs.server.service.directmessage;

import kr.co.amateurs.server.annotation.alarmtrigger.AlarmTrigger;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectMessageService {
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageRoomRepository directMessageRoomRepository;

    @AlarmTrigger(type = AlarmType.DIRECT_MESSAGE)
    public DirectMessageResponse saveMessage(String roomId, DirectMessageRequest request) {
        DirectMessageRoom room = validateRoomAccess(roomId, request.senderId());

        DirectMessage message = directMessageRepository.save(request.toCollection(roomId));
        updateLastMessage(room, message.getContent());
        return DirectMessageResponse.fromCollection(message);
    }

    public DirectMessageRoomResponse createRoom(DirectMessageRoomCreateRequest request) {
        List<Long> ids = request.participantMap().keySet().stream().sorted().toList();

        DirectMessageRoom room = directMessageRoomRepository.findRoomByUserIds(ids.get(0), ids.get(1))
                .map(this::reEntryParticipants)
                .orElseGet(() -> directMessageRoomRepository.save(request.toCollection()));
        Long currentUserId = 1L;
        return DirectMessageRoomResponse.fromCollection(room, currentUserId);
    }

    public DirectMessageRoom findRoomById(String roomId) {
        return directMessageRoomRepository.findById(roomId)
                .orElseThrow(ErrorCode.NOT_FOUND_ROOM);
    }

    public List<DirectMessageRoomResponse> getRooms(Long userId) {
        List<DirectMessageRoom> rooms = directMessageRoomRepository.findActiveRoomsByUserId(userId);
        return rooms.stream()
                .map(room -> DirectMessageRoomResponse.fromCollection(room, userId))
                .toList();
    }

    public DirectMessagePageResponse getMessages(DirectMessagePaginationParam param) {
        DirectMessageRoom room = validateRoomAccess(param.getRoomId(), param.getUserId());
        LocalDateTime userLeftAt = room.getParticipantLeftAt(param.getUserId());
        Page<DirectMessage> page = getMessagesByRoomId(param, userLeftAt);
        return DirectMessagePageResponse.from(page);
    }

    public void exitRoom(DirectMessageRoomExitRequest request) {
        DirectMessageRoom room = validateRoomAccess(request.roomId(), request.userId());
        room.userLeaveRoom(request.userId());

        if (room.allParticipantsLeft()) {
            directMessageRoomRepository.delete(room);
            directMessageRepository.deleteAllByRoomId(room.getId());
        } else {
            directMessageRoomRepository.save(room);
        }
    }

    /*
     * private method
     */
    private DirectMessageRoom validateRoomAccess(String roomId, Long userId) {
        DirectMessageRoom room = directMessageRoomRepository.findById(roomId)
                .orElseThrow(ErrorCode.NOT_FOUND_ROOM);

        if (!room.isParticipate(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_IN_ROOM);
        }

        return room;
    }

    private Page<DirectMessage> getMessagesByRoomId(DirectMessagePaginationParam pageParam, LocalDateTime afterTime) {
        return afterTime != null
                ? directMessageRepository.findByRoomIdAndSentAtAfterOrderBySentAtDesc(
                pageParam.getRoomId(), afterTime, pageParam.toPageable())
                : directMessageRepository.findByRoomIdOrderBySentAtDesc(
                pageParam.getRoomId(), pageParam.toPageable());
    }

    private DirectMessageRoom reEntryParticipants(DirectMessageRoom room) {
        boolean reEntry = room.getParticipants().stream()
                .filter(participant -> !participant.getIsActive())
                .peek(Participant::reEntry)
                .findAny()
                .isPresent();

        return reEntry ? directMessageRoomRepository.save(room) : room;
    }

    private void updateLastMessage(DirectMessageRoom room, String message) {
        room.updateLastMessage(message);
        directMessageRoomRepository.save(room);
    }
}
