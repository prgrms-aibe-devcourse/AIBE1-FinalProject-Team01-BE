package kr.co.amateurs.server.domain.entity.user.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사용자 관심 토픽")
public enum Topic {
    WEB("웹개발"),
    FRONTEND("프론트엔드"),
    BACKEND("백엔드"),
    FULLSTACK("풀스택"),
    MOBILE("모바일"),
    AI("AI/머신러닝"),
    DEVOPS("DevOps"),
    DATABASE("데이터베이스"),
    CLOUD("클라우드"),
    SECURITY("보안"),
    DATA("데이터 분석");




    private final String displayName;
}