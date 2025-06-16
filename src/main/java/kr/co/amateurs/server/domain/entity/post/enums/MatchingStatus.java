package kr.co.amateurs.server.domain.entity.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchingStatus {
    OPEN("열린"),
    CLOSED("닫힌"),
    MATCHED("매칭됨");

    private final String description;
}
