package kr.co.amateurs.server.repository.report;

import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("""
    SELECT r FROM Report r
    WHERE (:status IS NULL OR r.status = :status)
      AND (
           :target IS NULL
        OR (:target = "POST" AND r.post IS NOT NULL)
        OR (:target = "COMMENT" AND r.comment IS NOT NULL)
      )
    """)
    Page<Report> findByStatusAndType(@Param("status") ReportStatus status, @Param("target") String target, Pageable pageable);

    @Query("""
    SELECT r FROM Report r
    LEFT JOIN FETCH r.post
    LEFT JOIN FETCH r.comment
    WHERE r.id = :reportId
    """)
    Optional<Report> findByIdWithRelations(@Param("reportId") Long reportId);

    boolean existsByUserIdAndPostIdAndReportType(Long userId, Long postId, ReportType reportType);

    boolean existsByUserIdAndCommentIdAndReportType(Long userId, Long commentId, ReportType reportType);
}
