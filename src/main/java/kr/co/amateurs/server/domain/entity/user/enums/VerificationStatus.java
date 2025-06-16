package kr.co.amateurs.server.domain.entity.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationStatus {
    PENDING("대기중"),
    APPROVED("승인됨"),
    REJECTED("거부됨");

    private final String description;
}
