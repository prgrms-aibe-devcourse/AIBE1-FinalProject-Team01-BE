package kr.co.amateurs.server.domain.entity.directmessage;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "direct_message_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DirectMessageRoom {
    @Id
    private String id;
    private String roomName;
    private String lastMessage;

    @Builder.Default
    private List<Long> userIds = new ArrayList<>();

    @Builder.Default
    private List<UserReadStatus> readStatuses = new ArrayList<>();

    public void updateLastMessage(String message) {
        this.lastMessage = message;
    }
}
