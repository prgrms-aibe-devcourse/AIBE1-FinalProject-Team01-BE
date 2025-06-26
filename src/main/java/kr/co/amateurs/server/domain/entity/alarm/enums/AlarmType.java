package kr.co.amateurs.server.domain.entity.alarm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    COMMENT("새로운 댓글이 작성되었습니다."),
    DIRECT_MESSAGE("새로운 DM이 도착했습니다.");

    private final String title;
}