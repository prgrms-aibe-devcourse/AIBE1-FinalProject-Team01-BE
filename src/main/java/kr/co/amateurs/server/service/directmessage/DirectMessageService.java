package kr.co.amateurs.server.service.directmessage;

import kr.co.amateurs.server.annotation.alarmtrigger.AlarmTrigger;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.dto.directmessage.event.AnonymizeEvent;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRoomRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectMessageService {
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageRoomRepository directMessageRoomRepository;

    private final UserService userService;
    private final FileService fileService;

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

    public DirectMessageRoom findRoomById(String roomId) {
        return directMessageRoomRepository.findById(roomId)
                .orElseThrow(ErrorCode.NOT_FOUND_ROOM);
    }

    public List<DirectMessageRoomResponse> getRooms() {
        User currentUser = userService.getCurrentLoginUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "sentAt");
        List<DirectMessageRoom> rooms = directMessageRoomRepository.findActiveRoomsByUserId(currentUser.getId(), sort);
        return rooms.stream()
                .map(room -> DirectMessageRoomResponse.fromCollection(room, currentUser))
                .toList();
    }

    public DirectMessagePageResponse getMessages(DirectMessagePaginationParam param) {
        DirectMessageRoom room = validateRoomAccess(param.getRoomId(), param.getUserId());
        LocalDateTime userLeftAt = room.getParticipantReEntryAt(param.getUserId());
        Page<DirectMessage> page = getMessagesByRoomId(param, userLeftAt);
        return DirectMessagePageResponse.from(page);
    }

    @Async
    @EventListener
    public void anonymizeUser(AnonymizeEvent event) {
        User user = event.user();
        directMessageRepository.anonymizeUser(user.getId(), user.getNickname(), user.getImageUrl());
    }

    public void exitRoom(String roomId) {
        User currentUser = userService.getCurrentLoginUser();
        DirectMessageRoom room = validateRoomAccess(roomId, currentUser.getId());
        room.userLeaveRoom(currentUser.getId());

        if (room.allParticipantsLeft()) {
            directMessageRoomRepository.delete(room);
            directMessageRepository.deleteAllByRoomId(roomId);
            directMessageRepository.findByRoomIdAndMessageTypeIn(roomId, List.of(MessageType.FILE, MessageType.IMAGE))
                    .forEach(message -> fileService.deleteFile(message.getContent()));
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
