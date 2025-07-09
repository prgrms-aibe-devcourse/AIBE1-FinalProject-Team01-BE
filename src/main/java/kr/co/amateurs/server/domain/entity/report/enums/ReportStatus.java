package kr.co.amateurs.server.domain.entity.report.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("대기 중"),
    PROCESSING("처리 중"),
    RESOLVED("처리 완료 (위반)"),
    REJECTED("처리 완료 (정상)"),
    ERROR("처리 오류"),
    MANUAL_REVIEW("수동 검토 필요");

    private final String description;
}