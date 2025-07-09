package kr.co.amateurs.server.domain.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.domain.entity.report.enums.ReportTarget;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;

public record ReportRequestDTO(
        @Schema(
                description = "신고 대상 ID (게시글 ID 또는 댓글 ID)",
                example = "123"
        )
        @NotNull(message = "targetId는 필수입니다.")
        Long targetId,
        @Schema(
                description = "신고 대상",
                example = "POST",
                allowableValues = {"POST", "COMMENT"}
        )
        @NotNull(message = "ReportTarget 은 필수입니다.")
        ReportTarget reportTarget,
        @Schema(
                description = "신고 사유",
                example = "욕설 신고합니다."
        )
        @NotBlank(message = "설명은 필수이며 공백일 수 없습니다.")
        String description,
        @Schema(
                description = "신고 타입",
                example = "SPAM"
        )
        @NotNull(message = "ReportType 은 필수입니다.")
        ReportType reportType
) {}
