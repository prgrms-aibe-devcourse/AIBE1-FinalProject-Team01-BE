package kr.co.amateurs.server.domain.entity.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    COMMENT("댓글", "새로운 댓글이 달렸습니다."),
    REPLY("대댓글", "새로운 대댓글이 달렸습니다."),
    FOLLOW("팔로우", "새로운 팔로워가 생겼습니다."),
    DM("쪽지", "새로운 쪽지가 도착했습니다."),
    LIKE("좋아요", "게시글에 좋아요가 눌렸습니다."),
    BOOKMARK("북마크", "게시글이 북마크되었습니다.");

    private final String title;
    private final String defaultMessage;
}
