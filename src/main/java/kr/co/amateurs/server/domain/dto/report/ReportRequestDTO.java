package kr.co.amateurs.server.domain.dto.report;

import jakarta.validation.constraints.NotBlank;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;

public record ReportRequestDTO(
        Long reportId,
        ReportType reportType,
        @NotBlank(message = "설명은 필수이며 공백일 수 없습니다")
        String description
) {}
