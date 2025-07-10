package kr.co.amateurs.server.service.directmessage;

import kr.co.amateurs.server.annotation.alarmtrigger.AlarmTrigger;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRoomRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DirectMessageService {
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageRoomRepository directMessageRoomRepository;

    private final UserService userService;

    //test
    private final UserRepository userRepository;
    
    private final Random random = new Random();

    @AlarmTrigger(type = AlarmType.DIRECT_MESSAGE)
    public DirectMessageResponse saveMessage(String roomId, DirectMessageRequest request) {
        DirectMessageRoom room = validateRoomAccess(roomId, request.senderId());

        DirectMessage message = directMessageRepository.save(request.toCollection(roomId));
        updateLastMessage(room, message.getContent());
        return DirectMessageResponse.fromCollection(message);
    }

    public DirectMessageRoomResponse createRoom(long partnerId) {
        User currentUser = userService.getCurrentLoginUser();
        if (currentUser.getId().equals(partnerId)) {
            throw new CustomException(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }

        User partner = userService.findById(partnerId);
        List<User> participants = List.of(partner, currentUser);

        DirectMessageRoom room = directMessageRoomRepository.findRoomByUserIds(currentUser.getId(), partnerId)
                .map(this::reEntryParticipants)
                .orElseGet(() -> directMessageRoomRepository.save(DirectMessageRoom.from(participants)));

        return DirectMessageRoomResponse.fromCollection(room, currentUser);
    }

    public DirectMessageRoomResponse createTestRoom() {
        User currentUser = userService.getCurrentLoginUser();
        
        List<Long> existingPartnerIds = directMessageRoomRepository.findAllRoomsByUserId(currentUser.getId()).stream()
                .flatMap(room -> room.getParticipants().stream())
                .map(Participant::getUserId)
                .filter(id -> !id.equals(currentUser.getId()))
                .distinct()
                .toList();
        
        List<User> availableUsers = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .filter(user -> !existingPartnerIds.contains(user.getId()))
                .toList();
        
        if (availableUsers.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }
        
        User partner = availableUsers.get(random.nextInt(availableUsers.size()));
        
        // 새 채팅방 생성
        List<User> participants = List.of(currentUser, partner);
        DirectMessageRoom newRoom = directMessageRoomRepository.save(DirectMessageRoom.from(participants));
        
        return DirectMessageRoomResponse.fromCollection(newRoom, partner);
    }

    public DirectMessageRoom findRoomById(String roomId) {
        return directMessageRoomRepository.findById(roomId)
                .orElseThrow(ErrorCode.NOT_FOUND_ROOM);
    }

    public List<DirectMessageRoomResponse> getRooms() {
        User currentUser = userService.getCurrentLoginUser();
        List<DirectMessageRoom> rooms = directMessageRoomRepository.findActiveRoomsByUserId(currentUser.getId());
        return rooms.stream()
                .sorted(Comparator.comparing(DirectMessageRoom::getSentAt).reversed())
                .map(room -> DirectMessageRoomResponse.fromCollection(room, currentUser))
                .toList();
    }

    public DirectMessagePageResponse getMessages(DirectMessagePaginationParam param) {
        DirectMessageRoom room = validateRoomAccess(param.getRoomId(), param.getUserId());
        LocalDateTime userLeftAt = room.getParticipantLeftAt(param.getUserId());
        Page<DirectMessage> page = getMessagesByRoomId(param, userLeftAt);
        return DirectMessagePageResponse.from(page);
    }

    public void exitRoom(String roomId) {
        User currentUser = userService.getCurrentLoginUser();
        DirectMessageRoom room = validateRoomAccess(roomId, currentUser.getId());
        room.userLeaveRoom(currentUser.getId());

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
