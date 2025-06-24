package kr.co.amateurs.server.repository.report;

import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("""
    SELECT r FROM Report r
    WHERE (:status IS NULL OR r.status = :status)
      AND (
           :type IS NULL
        OR (:type = "POST" AND r.post IS NOT NULL)
        OR (:type = "COMMENT" AND r.comment IS NOT NULL)
      )
    """)
    Page<Report> findByStatusAndType(@Param("status") ReportStatus status, @Param("type") String type, Pageable pageable);
}
