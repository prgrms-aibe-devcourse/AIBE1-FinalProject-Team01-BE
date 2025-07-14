package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.domain.dto.report.QueueStatus;
import kr.co.amateurs.server.service.report.processor.ReportProcessingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class ReportProcessingManagerTest {

    @InjectMocks
    private ReportProcessingManager reportProcessingManager;

    private BlockingQueue<Long> testQueue;
    private AtomicBoolean testRunning;

    @BeforeEach
    void setUp() {
        testQueue = new LinkedBlockingQueue<>();
        testRunning = new AtomicBoolean(false);

        ReflectionTestUtils.setField(reportProcessingManager, "processingQueue", testQueue);
        ReflectionTestUtils.setField(reportProcessingManager, "running", testRunning);
    }

    @Test
    void 큐에_신고_ID_추가_성공() {
        // given
        Long reportId = 1L;

        // when
        reportProcessingManager.addToQueue(reportId);

        // then
        assertThat(testQueue.size()).isEqualTo(1);
        assertThat(testQueue.contains(reportId)).isTrue();
    }

    @Test
    void 큐_상태_조회() {
        // given
        testQueue.add(1L);
        testQueue.add(2L);
        testRunning.set(true);

        // when
        QueueStatus status = reportProcessingManager.getQueueStatus();

        // then
        assertThat(status.queueSize()).isEqualTo(2);
        assertThat(status.isRunning()).isTrue();
    }

    @Test
    void 큐에_여러_신고_추가_및_크기_확인() {
        // given
        Long reportId1 = 1L;
        Long reportId2 = 2L;
        Long reportId3 = 3L;

        // when
        reportProcessingManager.addToQueue(reportId1);
        reportProcessingManager.addToQueue(reportId2);
        reportProcessingManager.addToQueue(reportId3);

        // then
        QueueStatus status = reportProcessingManager.getQueueStatus();
        assertThat(status.queueSize()).isEqualTo(3);
        assertThat(testQueue.contains(reportId1)).isTrue();
        assertThat(testQueue.contains(reportId2)).isTrue();
        assertThat(testQueue.contains(reportId3)).isTrue();
    }

    @Test
    void 빈_큐_상태_확인() {
        // given
        testRunning.set(false);

        // when
        QueueStatus status = reportProcessingManager.getQueueStatus();

        // then
        assertThat(status.queueSize()).isEqualTo(0);
        assertThat(status.isRunning()).isFalse();
    }

    @Test
    void 큐가_가득_찬_경우_처리() {
        // given
        BlockingQueue<Long> limitedQueue = new LinkedBlockingQueue<>(2);
        ReflectionTestUtils.setField(reportProcessingManager, "processingQueue", limitedQueue);

        // when
        reportProcessingManager.addToQueue(1L);
        reportProcessingManager.addToQueue(2L);
        reportProcessingManager.addToQueue(3L);

        // then
        QueueStatus status = reportProcessingManager.getQueueStatus();
        assertThat(status.queueSize()).isEqualTo(2);
    }

    @Test
    void 매니저_초기화_상태_확인() {
        // given & when
        QueueStatus initialStatus = reportProcessingManager.getQueueStatus();

        // then
        assertThat(initialStatus.queueSize()).isEqualTo(0);
        assertThat(initialStatus.isRunning()).isFalse();
    }

    @Test
    void 동일한_신고_ID_중복_추가() {
        // given
        Long reportId = 1L;

        // when
        reportProcessingManager.addToQueue(reportId);
        reportProcessingManager.addToQueue(reportId);
        reportProcessingManager.addToQueue(reportId);

        // then
        QueueStatus status = reportProcessingManager.getQueueStatus();
        assertThat(status.queueSize()).isEqualTo(3);
    }

    @Test
    void 큐_상태_스레드_정보_확인() {
        // given
        testRunning.set(true);

        // when
        QueueStatus status = reportProcessingManager.getQueueStatus();

        // then
        assertThat(status.isRunning()).isTrue();
        assertThat(status.isThreadAlive()).isFalse();
    }

    @Test
    void 큐_크기_변화_모니터링() {
        // given
        reportProcessingManager.addToQueue(1L);
        QueueStatus status1 = reportProcessingManager.getQueueStatus();

        reportProcessingManager.addToQueue(2L);
        reportProcessingManager.addToQueue(3L);
        QueueStatus status2 = reportProcessingManager.getQueueStatus();

        testQueue.poll();
        QueueStatus status3 = reportProcessingManager.getQueueStatus();

        // then
        assertThat(status1.queueSize()).isEqualTo(1);
        assertThat(status2.queueSize()).isEqualTo(3);
        assertThat(status3.queueSize()).isEqualTo(2);
    }

    @Test
    void 큐_상태_변경_후_조회() {
        // given
        testRunning.set(false);
        QueueStatus initialStatus = reportProcessingManager.getQueueStatus();

        testRunning.set(true);
        testQueue.add(1L);
        testQueue.add(2L);
        QueueStatus updatedStatus = reportProcessingManager.getQueueStatus();

        // then
        assertThat(initialStatus.queueSize()).isEqualTo(0);
        assertThat(initialStatus.isRunning()).isFalse();

        assertThat(updatedStatus.queueSize()).isEqualTo(2);
        assertThat(updatedStatus.isRunning()).isTrue();
    }

    @Test
    void 큐_순서_확인() {
        // given
        Long reportId1 = 1L;
        Long reportId2 = 2L;
        Long reportId3 = 3L;

        // when
        reportProcessingManager.addToQueue(reportId1);
        reportProcessingManager.addToQueue(reportId2);
        reportProcessingManager.addToQueue(reportId3);

        // then
        assertThat(testQueue.poll()).isEqualTo(reportId1);
        assertThat(testQueue.poll()).isEqualTo(reportId2);
        assertThat(testQueue.poll()).isEqualTo(reportId3);
        assertThat(testQueue.poll()).isNull();
    }

    @Test
    void 큐_상태_정보_객체_생성_확인() {
        // given
        testQueue.add(1L);
        testQueue.add(2L);
        testRunning.set(true);

        // when
        QueueStatus status = reportProcessingManager.getQueueStatus();

        // then
        assertThat(status).isNotNull();
        assertThat(status.queueSize()).isNotNull();
        assertThat(status.isRunning()).isNotNull();
        assertThat(status.isThreadAlive()).isNotNull();
    }

    @Test
    void 대량_신고_ID_추가_성능_테스트() {
        // given
        int largeSize = 1000;

        // when
        for (int i = 0; i < largeSize; i++) {
            reportProcessingManager.addToQueue((long) i);
        }

        // then
        QueueStatus status = reportProcessingManager.getQueueStatus();
        assertThat(status.queueSize()).isEqualTo(largeSize);
    }

    @Test
    void 음수_신고_ID_추가() {
        // given
        Long negativeReportId = -1L;

        // when
        reportProcessingManager.addToQueue(negativeReportId);

        // then
        QueueStatus status = reportProcessingManager.getQueueStatus();
        assertThat(status.queueSize()).isEqualTo(1);
        assertThat(testQueue.contains(negativeReportId)).isTrue();
    }
}

