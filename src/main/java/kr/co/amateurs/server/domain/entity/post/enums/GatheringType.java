package kr.co.amateurs.server.domain.entity.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatheringType {
    STUDY("스터디"),
    SIDE_PROJECT("사이드 프로젝트");

    private final String description;
}
