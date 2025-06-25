package kr.co.amateurs.server.domain.entity.alarm;

import jakarta.persistence.Id;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "alarms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Alarm {
    @Id
    private String id;

    @Indexed
    private long userId;

    private AlarmType type;
    private AlarmMeta metaData;
    private String content;

    @Builder.Default
    private boolean isRead = false;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    public interface AlarmMeta {
    }

    static class CommentMeta implements AlarmMeta {
        private Long postId;
        private Long commentId;
    }

    static class DirectMessageMeta implements AlarmMeta {
        private Long roomId;
        private Long messageId;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
