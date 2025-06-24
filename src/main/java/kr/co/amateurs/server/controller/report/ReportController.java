package kr.co.amateurs.server.controller.report;

import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.dto.report.ReportResponseDTO;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ReportResponseDTO>> getReports(
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReportResponseDTO> reports = reportService.getReports(reportType, status, page, size);
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @PostMapping
    public ResponseEntity<ReportResponseDTO> createReport(
            @Valid @RequestBody ReportRequestDTO requestDTO
    ){
        ReportResponseDTO report = reportService.createReport(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{reportId}/{status}")
    public ResponseEntity<Void> updateStatusReport(
            @PathVariable Long reportId,
            @PathVariable ReportStatus status
    ){
        reportService.updateStatusReport(reportId, status);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
