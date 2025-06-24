package kr.co.amateurs.server.domain.entity.alarm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    COMMENT("댓글 알람"),
    DIRECT_MESSAGE("DM 알람");

    private final String value;
}