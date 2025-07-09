package kr.co.amateurs.server.domain.dto.report;

import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;

public record ReportResponseDTO (
    Long id,
    Long postId,
    String postTitle,
    String postContent,
    String postAuthor,
    Long commentId,
    String commentContent,
    String commentAuthor,
    String reporterName,
    String description,
    ReportStatus reportStatus,
    ReportType reportType
){
    public static ReportResponseDTO from(Report report) {
        return new ReportResponseDTO(
                report.getId(),
                report.getPost() != null ? report.getPost().getId() : null,
                report.getPost() != null ? report.getPost().getTitle() : null,
                report.getPost() != null ? report.getPost().getContent() : null,
                report.getPost() != null ? report.getPost().getUser().getNickname() : null,
                report.getComment() != null ? report.getComment().getId() : null,
                report.getComment() != null ? report.getComment().getContent() : null,
                report.getComment() != null ? report.getComment().getUser().getNickname() : null,
                report.getUser().getNickname(),
                report.getDescription(),
                report.getStatus(),
                report.getReportType()
        );
    }
}
