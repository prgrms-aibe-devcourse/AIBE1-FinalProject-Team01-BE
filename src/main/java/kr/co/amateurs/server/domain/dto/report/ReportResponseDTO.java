package kr.co.amateurs.server.domain.dto.report;

import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;

public record ReportResponseDTO (
    Post post,
    Comment comment,
    String username,
    String description,
    ReportStatus ReportType
){
    public static ReportResponseDTO from(Report report) {
        return new ReportResponseDTO(
                report.getPost(),
                report.getComment(),
                report.getUser().getNickname(),
                report.getDescription(),
                report.getStatus()
        );
    }
}
