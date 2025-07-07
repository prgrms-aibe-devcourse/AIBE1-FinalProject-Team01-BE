package kr.co.amateurs.server.service.report.handler;

import kr.co.amateurs.server.domain.entity.report.Report;

public abstract class ReportTargetHandler {
    /**
     * 신고 대상이 이미 블라인드 처리되었는지 확인
     */
    public abstract boolean isAlreadyBlinded(Report report);

    /**
     * 신고 대상 ID 반환
     */
    public abstract Long getTargetId(Report report);

    /**
     * 신고 대상을 블라인드 처리
     */
    public abstract void blindTarget(Report report);

    /**
     * 신고 대상 타입 문자열 반환
     */
    public abstract String getTargetType();
}