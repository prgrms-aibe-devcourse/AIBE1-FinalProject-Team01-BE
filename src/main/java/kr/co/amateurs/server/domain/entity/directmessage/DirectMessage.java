package kr.co.amateurs.server.domain.entity.directmessage;

import jakarta.persistence.Id;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "direct_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DirectMessage {
    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private String receiverId;
    private String content;
    private final MessageType messageType = MessageType.TEXT;

    @Indexed
    private LocalDateTime sentAt = LocalDateTime.now();
    private LocalDateTime readAt;
}
