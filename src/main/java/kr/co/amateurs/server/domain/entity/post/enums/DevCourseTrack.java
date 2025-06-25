package kr.co.amateurs.server.domain.entity.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DevCourseTrack {
    FRONTEND("프론트엔드"),
    BACKEND("백엔드"),
    AI_BACKEND("AI 백엔드"),
    FULL_STACK("풀스택"),
    DATA_SCIENCE("데이터 분석"),
    DATA_ENGINEERING("데이터 엔지니어링");

    private final String description;
}

