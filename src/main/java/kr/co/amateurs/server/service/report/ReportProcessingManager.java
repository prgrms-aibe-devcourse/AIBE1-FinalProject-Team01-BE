package kr.co.amateurs.server.service.report;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.report.LLMAnalysisResult;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.repository.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportProcessingManager {

    private final ReportRepository reportRepository;
    private final ReportProcessor reportProcessor;

    private final BlockingQueue<Long> processingQueue = new LinkedBlockingQueue<>();

    private Map<ReportType, ReportProcessor> processorMap;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread processingThread;

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

    /**
     * 신고를 처리 큐에 추가
     */
    public void addToQueue(Long reportId) {
        try {
            processingQueue.offer(reportId);
            log.info("신고 큐 추가 완료 - Report ID: {}, 현재 큐 크기: {}",
                    reportId, processingQueue.size());
        } catch (Exception e) {
            log.error("신고 큐 추가 실패 - Report ID: {}", reportId, e);
        }
    }

    /**
     * 현재 큐 상태 조회
     */
    public QueueStatus getQueueStatus() {
        return new QueueStatus(
                processingQueue.size(),
                running.get(),
                processingThread != null && processingThread.isAlive()
        );
    }

    /**
     * 처리 스레드 시작
     */
    private void startProcessingThread() {
        if (running.compareAndSet(false, true)) {
            processingThread = new Thread(this::processReports, "report-processor");
            processingThread.setDaemon(true);
            processingThread.start();
            log.info("신고 처리 스레드 시작됨");
        }
    }

    /**
     * 처리 스레드 중지
     */
    private void stopProcessingThread() {
        if (running.compareAndSet(true, false)) {
            if (processingThread != null) {
                processingThread.interrupt();
                try {
                    processingThread.join(5000); // 5초 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("신고 처리 스레드 중지됨");
        }
    }

    /**
     * 메인 처리 루프 - FIFO 순서로 처리
     */
    private void processReports() {
        log.info("신고 처리 루프 시작");

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // FIFO 큐에서 신고 ID 가져오기 (블로킹)
                Long reportId = processingQueue.take();

                log.info("신고 처리 시작 - Report ID: {}", reportId);
                processReport(reportId);

                // 처리 간격 조절 (1초 대기)
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                log.info("신고 처리 스레드 인터럽트됨");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("신고 처리 중 예상치 못한 오류 발생", e);
                // 오류 발생해도 계속 처리
            }
        }

        log.info("신고 처리 루프 종료");
    }

    /**
     * 개별 신고 처리
     */
    private void processReport(Long reportId) {
        try {
            // 신고 조회
            Report report = reportRepository.findByIdWithRelations(reportId)
                    .orElseThrow(ErrorCode.REPORT_NOT_FOUND);

            if (report.getStatus() != ReportStatus.PENDING) {
                log.warn("이미 처리된 신고 - Report ID: {}, 상태: {}",
                        reportId, report.getStatus());
                return;
            }

            // 처리 시작 상태로 변경
            report.startProcessing();

            reportRepository.save(report);

            log.info("신고 처리 시작 - Report ID: {}, Type: {}",
                    reportId, report.getReportType());

            // 실제 처리 수행
            LLMAnalysisResult result = reportProcessor.process(report);

            handleProcessingResult(report, result);

        } catch (Exception e) {
            log.error("신고 처리 실패 - Report ID: {}", reportId, e);
            try {
                Report report = reportRepository.findById(reportId).orElse(null);
                if (report != null) {
                    handleProcessingError(report, "처리 중 예외 발생: " + e.getMessage());
                }
            } catch (Exception saveError) {
                log.error("신고 오류 상태 저장 실패 - Report ID: {}", reportId, saveError);
            }
        }
    }

    /**
     * 처리 성공 시 처리
     */
    private void handleProcessingResult(Report report, LLMAnalysisResult result) {
        try {
            // 신뢰도가 낮으면 수동 검토 필요
            if (result.needsManualReview()) {
                report.updateStatusReport(ReportStatus.MANUAL_REVIEW);
                log.info("수동 검토 필요 - Report ID: {}, 신뢰도: {}",
                        report.getId(), result.confidenceScore());
            } else {
                report.completeProcessing(
                        result.isViolation(),
                        result.reason(),
                        result.confidenceScore()
                );

                log.info("신고 처리 완료 - Report ID: {}, 위반여부: {}, 신뢰도: {}",
                        report.getId(), result.isViolation(), result.confidenceScore());
            }

            reportRepository.save(report);

        } catch (Exception e) {
            log.error("처리 결과 저장 실패 - Report ID: {}", report.getId(), e);
        }
    }

    /**
     * 처리 실패 시 처리
     */
    private void handleProcessingError(Report report, String errorMessage) {
        try {
            report.completeProcessing(false, errorMessage, null);
            reportRepository.save(report);

            log.error("신고 처리 오류 - Report ID: {}, 오류: {}",
                    report.getId(), errorMessage);

        } catch (Exception e) {
            log.error("처리 오류 결과 저장 실패 - Report ID: {}", report.getId(), e);
        }
    }

    /**
     * 큐 상태 정보
     */
    public record QueueStatus(
            int queueSize,
            boolean isRunning,
            boolean isThreadAlive
    ) {}
}
