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
public class UserReadStatus {
    private Long userId;
    private LocalDateTime lastReadAt;

    public void updateLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public UserReadStatus(Long userId){
        this.userId = userId;
        this.lastReadAt = null;
    }
}
