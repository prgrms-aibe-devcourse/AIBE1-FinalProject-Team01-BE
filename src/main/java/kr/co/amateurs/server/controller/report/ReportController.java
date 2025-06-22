package kr.co.amateurs.server.controller.report;

import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.dto.report.ReportResponseDTO;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    // TODO 페이징 처리가 필요하면 추가 예정
    // @PreAuthorize 어드민
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getAllReports() {
        List<ReportResponseDTO> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    // @PreAuthorize 어드민
    @GetMapping("/{status}")
    public ResponseEntity<List<ReportResponseDTO>> getReportsByStatus(
            @PathVariable ReportStatus status
    ) {
        List<ReportResponseDTO> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    // @PReAuthorize 일반 유저도 가능 ???
    @PostMapping
    public ResponseEntity<ReportResponseDTO> createReport(
            @Valid @RequestBody ReportRequestDTO requestDTO
    ){
        ReportResponseDTO report = reportService.createReport(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    // @PreAuthorize 어드민
    @PutMapping("/{reportId}/{status}")
    public ResponseEntity<ReportResponseDTO> updateStatusReport(
            @PathVariable Long reportId,
            @PathVariable ReportStatus status
    ){
        reportService.updateStatusReport(reportId, status);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<ReportResponseDTO> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
