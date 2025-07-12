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
    MOBILE("Mobile"),
    AI("AI/ML"),
    DEVOPS("DevOps"),
    DATA("Data Science"),
    FULLSTACK("Full Stack"),
    ALGORITHM("Algorithm"),
    ANDROID("Android"),
    IOS("iOS"),
    GAME_DEV("Game Dev"),
    LLM("LLM"),
    DATABASE("Database"),
    BUILD_SEC("Build&Sec"),
    CLOUD("Cloud"),
    SECURITY("Security"),
    DESIGN("Design"),
    WEB("Web");

    private final String displayName;
}