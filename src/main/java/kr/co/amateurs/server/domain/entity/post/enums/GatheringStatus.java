package kr.co.amateurs.server.domain.entity.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatheringStatus {
    RECRUITING("모집중"),
    COMPLETED("모집완료");

    private final String description;
}
