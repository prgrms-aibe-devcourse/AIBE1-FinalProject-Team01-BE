package kr.co.amateurs.server.domain.dto.directmessage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import kr.co.amateurs.server.domain.entity.directmessage.Participant;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record DirectMessageRoomCreateRequest(
        @Schema(
                description = "참여할 사용자들 (userId: nickname 형태)",
                example = "{\"1\": \"김철수\", \"2\": \"이영희\"}",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Size(min = 2, max = 2, message = "DM 방에는 최소 2명 이상의 사용자가 필요합니다")
        Map<Long, String> participantMap
) {
    public DirectMessageRoom toCollection() {
        List<Participant> participants = participantMap.entrySet()
                .stream()
                .map(participant -> Participant.builder()
                        .userId(participant.getKey())
                        .nickname(participant.getValue())
                        .build())
                .toList();

        return DirectMessageRoom.builder()
                .participants(participants)
                .build();
    }
}
