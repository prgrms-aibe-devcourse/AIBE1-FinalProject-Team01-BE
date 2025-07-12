package kr.co.amateurs.server.domain.entity.user.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사용자 관심 토픽")
public enum Topic {
    FRONTEND("Frontend"),
    BACKEND("Backend"),
    DEVOPS("DevOps"),
    AI_CC("AI/CC"),
    ALGORITHM("Algorithm"),
    ANDROID("Android"),
    IOS("iOS"),
    GAME_DEV("게임개발"),
    LLM("LLM"),
    WEB("WEB"),
    DATA_SCIENCE("Data Science"),
    DB("DB"),
    BUILD_SEC("Build&Sec"),
    DESIGN("Design");

    private final String displayName;
}