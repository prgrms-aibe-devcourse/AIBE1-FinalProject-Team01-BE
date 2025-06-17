package kr.co.amateurs.server.domain.entity.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchingStatus {
    OPEN("매칭 중"),
    MATCHED("매칭 완료");

    private final String description;
}
