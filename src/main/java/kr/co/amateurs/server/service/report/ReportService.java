package kr.co.amateurs.server.service.report;

import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.dto.report.ReportResponseDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final UserService userService;

    public Page<ReportResponseDTO> getReports(ReportType reportType, ReportStatus status ,int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String typeName = (reportType != null) ? reportType.name() : null;
        Page<Report> reports = reportRepository.findByStatusAndType(status, typeName, pageable);

        return reports.map(ReportResponseDTO::from);
    }

    @Transactional
    public ReportResponseDTO createReport(ReportRequestDTO requestDTO) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.NOT_FOUND);

        Report report = createReportEntity(requestDTO, user);
        Report savedReport = reportRepository.save(report);

        return ReportResponseDTO.from(savedReport);
    }

    @Transactional
    public void updateStatusReport(Long reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId).orElseThrow(ErrorCode.NOT_FOUND);

        report.updateStatusReport(status);
    }

    @Transactional
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(ErrorCode.NOT_FOUND);

        reportRepository.delete(report);
    }

    private Report createReportEntity(ReportRequestDTO requestDTO, User user) {
        return switch (requestDTO.reportType()) {
            case POST -> {
                Post post = postRepository.findById(requestDTO.reportId())
                        .orElseThrow(ErrorCode.NOT_FOUND);
                yield Report.fromPost(post, user, requestDTO.description());
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(requestDTO.reportId())
                        .orElseThrow(ErrorCode.NOT_FOUND);
                yield Report.fromComment(comment, user, requestDTO.description());
            }
        };
    }
}
