package kr.co.amateurs.server.service.report.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.report.LLMAnalysisResult;
import kr.co.amateurs.server.domain.dto.report.QueueStatus;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.service.report.handler.ReportTargetHandler;
import kr.co.amateurs.server.service.report.handler.ReportTargetHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportProcessingManager {

    private final ReportRepository reportRepository;
    private final ReportProcessor reportProcessor;
    private final ReportTargetHandlerFactory handlerFactory;

    private final BlockingQueue<Long> processingQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread processingThread;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RATE_LIMIT_WAIT_MS = 60000;
    private static final long RETRY_WAIT_MS = 5000;
    private static final long WAIT_NEXT_MS = 1000;

    @PostConstruct
    public void initialize() {
        log.info("신고 처리 관리자 초기화 시작");

        startProcessingThread();

        log.info("신고 처리 관리자 초기화 완료");
    }

    @PreDestroy
    public void destroy() {
        stopProcessingThread();
        log.info("신고 처리 관리자 종료 완료");
    }

    public void addToQueue(Long reportId) {
        boolean success = processingQueue.offer(reportId);
        if (success) {
            log.info("신고 큐 추가 완료 - Report ID: {}, 현재 큐 크기: {}",
                    reportId, processingQueue.size());
        } else {
            log.warn("신고 큐가 가득 찼습니다 - Report ID: {}", reportId);
        }
    }

    public QueueStatus getQueueStatus() {
        return new QueueStatus(
                processingQueue.size(),
                running.get(),
                processingThread != null && processingThread.isAlive()
        );
    }

    private void startProcessingThread() {
        if (running.compareAndSet(false, true)) {
            processingThread = new Thread(this::processReports, "report-processor");
            processingThread.setDaemon(true);
            processingThread.start();
        }
    }

    private void stopProcessingThread() {
        if (running.compareAndSet(true, false)) {
            if (processingThread != null) {
                processingThread.interrupt();
                try {
                    processingThread.join(RETRY_WAIT_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processReports() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Long reportId = processingQueue.take();

                processReportWithRetry(reportId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("신고 처리 중 예상치 못한 오류 발생", e);
            }
        }
    }

    private void processReportWithRetry(Long reportId) {
        int attempt = 0;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                processReport(reportId);
                Thread.sleep(WAIT_NEXT_MS);
                return;

            } catch (Exception e) {
                attempt++;

                ///  429 에러 발생 시 1분 대기 후 다시 시작
                if (isRateLimitException(e)) {
                    log.warn("Rate limit 에러 발생");

                    waitForRateLimit();

                } else if (attempt < MAX_RETRY_ATTEMPTS) {
                    waitForRetry();
                } else {
                    handleProcessingError(reportId, "최대 재시도 횟수 초과: " + e.getMessage());
                    break;
                }
            }
        }
    }

    private boolean isRateLimitException(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;

        return message.contains("429") ||
                message.contains("rate limit") ||
                message.contains("Too Many Requests") ||
                message.contains("quota exceeded");
    }

    private void waitForRateLimit() {
        try {
            Thread.sleep(RATE_LIMIT_WAIT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForRetry() {
        try {
            Thread.sleep(RETRY_WAIT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processReport(Long reportId) {
        try {
            Report report = reportRepository.findByIdWithRelations(reportId)
                    .orElseThrow(ErrorCode.REPORT_NOT_FOUND);

            if (!report.getStatus().equals(ReportStatus.PENDING)) {
                log.warn("이미 처리된 신고 - Report ID: {}, 상태: {}",
                        reportId, report.getStatus());

                handleAlreadyProcessedReport(report);
                return;
            }

            ReportTargetHandler handler = handlerFactory.getHandler(report.getReportTarget());

            if (handler.isAlreadyBlinded(report)) {
                log.warn("이미 블라인드 된 {} - Target ID: {}",
                        handler.getTargetType(), handler.getTargetId(report));

                handleAlreadyBlindedReport(report, handler);
                return;
            }

            report.startProcessing();
            reportRepository.save(report);

            LLMAnalysisResult result = reportProcessor.process(report);
            handleProcessingResult(report, result, handler);

        } catch (Exception e) {
            log.error("신고 처리 실패 - Report ID: {}", reportId, e);
            handleProcessingError(reportId, e.getMessage());
            throw e;
        }
    }

    private void handleProcessingResult(Report report, LLMAnalysisResult result,
                                        ReportTargetHandler handler) {
        if (result.needsManualReview()) {
            report.manualProcessing(result.reason(), result.confidenceScore());
        } else {
            report.completeProcessing(
                    result.isViolation(),
                    result.reason(),
                    result.confidenceScore()
            );

            if (result.isHighConfidence()) {
                Long targetId = handler.getTargetId(report);
                handler.blindTarget(report);

            }
        }

        reportRepository.save(report);
    }

    private void handleProcessingError(Long reportId, String errorMessage) {
        Report report = reportRepository.findById(reportId).orElseThrow(ErrorCode.REPORT_NOT_FOUND);
        report.errorProcessing(errorMessage);
        reportRepository.save(report);
    }

    private void handleAlreadyProcessedReport(Report report) {
        String reason = String.format("이미 처리된 신고입니다. 현재 상태: %s", report.getStatus());
        report.completeProcessing(false, reason, null);
        reportRepository.save(report);
    }

    private void handleAlreadyBlindedReport(Report report, ReportTargetHandler handler) {
        String reason = String.format("이미 블라인드 처리된 %s입니다. 대상 ID: %s",
                handler.getTargetType(), handler.getTargetId(report));

        report.completeProcessing(true, reason, 1.0);
        reportRepository.save(report);
    }
}