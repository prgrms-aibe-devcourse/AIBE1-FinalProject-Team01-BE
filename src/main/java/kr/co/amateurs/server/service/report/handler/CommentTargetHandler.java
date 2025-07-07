package kr.co.amateurs.server.service.report.handler;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentTargetHandler extends ReportTargetHandler {

    private final CommentRepository commentRepository;

    @Override
    public boolean isAlreadyBlinded(Report report) {
        return report.getComment() != null && report.getComment().getIsBlinded();
    }

    @Override
    public Long getTargetId(Report report) {
        return report.getComment().getId();
    }

    @Override
    public void blindTarget(Report report) {
        Comment comment = commentRepository.findById(report.getComment().getId())
                .orElseThrow(ErrorCode.NOT_FOUND);
        comment.updateBlinded(true);
        commentRepository.save(comment);
    }

    @Override
    public String getTargetType() {
        return "COMMENT";
    }
}