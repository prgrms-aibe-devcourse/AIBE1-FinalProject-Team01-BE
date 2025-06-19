package kr.co.amateurs.server.domain.entity.user.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사용자 관심 토픽")
public enum Topic {
    FRONTEND("프론트엔드"),
    BACKEND("백엔드"),
    MOBILE("모바일"),
    AI("AI/머신러닝"),
    DEVOPS("DevOps"),
    DATA("데이터 분석");

    private final String displayName;
}