package kr.co.amateurs.server.controller.report;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Report", description = "신고 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(
            summary = "신고 목록 조회 (관리자 전용)",
            description = "모든 신고 목록을 페이지네이션으로 조회합니다. 신고 타입과 상태로 필터링할 수 있습니다"
    )
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
    @Operation(
            summary = "신고 접수",
            description = "게시글 또는 댓글을 신고합니다. 로그인된 사용자만 신고할 수 있습니다."
    )
    public ResponseEntity<ReportResponseDTO> createReport(
            @Valid @RequestBody ReportRequestDTO requestDTO
    ){
        ReportResponseDTO report = reportService.createReport(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @Operation(
            summary = "신고 상태 변경 (관리자 전용)",
            description = "특정 신고의 상태를 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{reportId}/{status}")
    public ResponseEntity<Void> updateStatusReport(
            @PathVariable Long reportId,
            @PathVariable ReportStatus status
    ){
        reportService.updateStatusReport(reportId, status);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "신고 삭제 (관리자 전용)",
            description = "특정 신고를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
