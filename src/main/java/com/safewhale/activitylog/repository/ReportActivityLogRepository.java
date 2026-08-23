package com.safewhale.activitylog.repository;

import com.safewhale.activitylog.domain.ReportActivityLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportActivityLogRepository extends JpaRepository<ReportActivityLog, Long> {
    List<ReportActivityLog> findAllByReportIdOrderByCreatedAt(Long reportId);
}
