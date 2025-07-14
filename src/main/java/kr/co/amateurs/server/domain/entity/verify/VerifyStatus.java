package kr.co.amateurs.server.domain.entity.verify;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerifyStatus {
    PROCESSING("인증 처리 중"),
    PENDING("관리자 검토 중"),
    COMPLETED("인증 완료"),
    FAILED("인증 실패");
    private final String description;
} 