package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.ReportCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportEventListener {
    private final ReportProcessingManager processingManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportCreated(ReportCreatedEvent event) {
        processingManager.addToQueue(event.reportId());
    }
}
