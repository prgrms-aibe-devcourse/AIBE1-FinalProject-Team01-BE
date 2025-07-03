package kr.co.amateurs.server.domain.entity.report;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportTarget;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTarget reportTarget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    private LocalDateTime processingStartedAt;

    private LocalDateTime processingCompletedAt;

    private Boolean isViolation;

    @Column(length = 1000)
    private String violationReason;

    private Double confidenceScore;

    public void startProcessing() {
        this.status = ReportStatus.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }

    public void completeProcessing(Boolean isViolation, String violationReason, Double confidenceScore) {
        this.status = isViolation ? ReportStatus.RESOLVED : ReportStatus.REJECTED;
        this.processingCompletedAt = LocalDateTime.now();
        this.isViolation = isViolation;
        this.violationReason = violationReason;
        this.confidenceScore = confidenceScore;
    }


    public static Report fromPost(Post post, User user, ReportRequestDTO requestDTO) {
        return Report.builder()
                .user(user)
                .post(post)
                .description(requestDTO.description())
                .status(ReportStatus.PENDING)
                .reportType(requestDTO.reportType())
                .reportTarget(requestDTO.reportTarget())
                .build();
    }

    public static Report fromComment(Comment comment, User user, ReportRequestDTO requestDTO) {
        return Report.builder()
                .user(user)
                .comment(comment)
                .description(requestDTO.description())
                .status(ReportStatus.PENDING)
                .reportType(requestDTO.reportType())
                .reportTarget(requestDTO.reportTarget())
                .build();
    }

    public void updateStatusReport(ReportStatus status) {
        this.status = status;
    }
}
