package kr.co.amateurs.server.service.report.processor;

import kr.co.amateurs.server.domain.dto.report.ReportCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 트랜잭션 저장 이후에 프로세서 매니저에 등록하기 위한 이벤트 리스너
 *
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.processing.enabled", havingValue = "true", matchIfMissing = true)
public class ReportEventListener {
    private final ReportProcessingManager processingManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportCreated(ReportCreatedEvent event) {
        processingManager.addToQueue(event.reportId());
    }
}
