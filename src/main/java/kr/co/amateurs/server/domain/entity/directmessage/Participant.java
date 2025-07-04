package kr.co.amateurs.server.domain.entity.directmessage;

import kr.co.amateurs.server.domain.entity.user.User;
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
    private String profileImage;
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

    public static Participant from(User user) {
        return Participant.builder()
                .nickname(user.getNickname())
                .profileImage(user.getImageUrl())
                .build();
    }
}
