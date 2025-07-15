package kr.co.amateurs.server.domain.entity.directmessage;

import jakarta.persistence.Id;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "direct_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DirectMessage {
    @Id
    private String id;

    @Indexed
    private String roomId;
    private Long senderId;
    private String senderNickname;
    private String senderProfileImage;
    private String content;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Builder.Default
    private boolean isRead = false;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    public void markAsRead() {
        this.isRead = true;
    }
}
