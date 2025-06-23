package kr.co.amateurs.server.domain.entity.directmessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    private Long userId;
    private String nickname;
    private LocalDateTime lastReadAt;
    private LocalDateTime leftAt;

    @Builder.Default
    private Boolean isActive = true;

    public void updateLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public void exitRoom() {
        this.isActive = false;
        this.leftAt = LocalDateTime.now();
    }

    public void reEntry() {
        this.isActive = true;
    }
}
