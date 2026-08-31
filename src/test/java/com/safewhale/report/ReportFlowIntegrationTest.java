package com.safewhale.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.safewhale.activitylog.repository.ReportActivityLogRepository;
import com.safewhale.admin.domain.Admin;
import com.safewhale.admin.repository.AdminRepository;
import com.safewhale.admin_report.service.AdminReportService;
import com.safewhale.department.domain.Department;
import com.safewhale.department.repository.DepartmentRepository;
import com.safewhale.notification.repository.NotificationRepository;
import com.safewhale.report.domain.ReportStatus;
import com.safewhale.report.domain.RiskLevel;
import com.safewhale.report.repository.ReportRepository;
import com.safewhale.report.service.ReportService;
import com.safewhale.user.domain.User;
import com.safewhale.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportFlowIntegrationTest {
    @Autowired UserRepository userRepository;
    @Autowired DepartmentRepository departmentRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired ReportService reportService;
    @Autowired AdminReportService adminReportService;
    @Autowired AdminRepository adminRepository;
    @Autowired ReportActivityLogRepository activityRepository;
    @Autowired NotificationRepository notificationRepository;

    @Test
    void draftSubmitReviewAndResolveProducesHistoryAndNotifications() {
        User user = userRepository.save(new User("GOOGLE", "integration-google", "통합테스트 사용자"));
        Department department = departmentRepository.save(new Department("통합테스트 시설팀", "TEST_FACILITY"));

        Long reportId = reportService.createDraft(user.getId()).id();
        reportRepository.findById(reportId).orElseThrow().reassignDepartment(department);

        reportService.submit(reportId, user.getId());
        adminReportService.changeStatus(reportId, 100L, ReportStatus.REVIEWING);
        adminReportService.resolve(reportId, 100L, "현장 보수를 완료했습니다.");

        assertThat(reportRepository.findById(reportId).orElseThrow().getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(activityRepository.findAllByReportIdOrderByCreatedAt(reportId)).hasSize(3);
        assertThat(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())).hasSize(3);
    }

    @Test
    void highRiskSubmitNotifiesReporterAndEveryAdmin() {
        User user = userRepository.save(new User("GOOGLE", "high-risk-google", "고위험 신고자"));
        Department department = departmentRepository.save(new Department("고위험 시설팀", "HIGH_RISK_FACILITY"));
        Admin firstAdmin = adminRepository.save(new Admin("high-risk-admin-1", "hash", "관리자1", department));
        Admin secondAdmin = adminRepository.save(new Admin("high-risk-admin-2", "hash", "관리자2", department));

        Long reportId = reportService.createDraft(user.getId()).id();
        reportRepository.findById(reportId).orElseThrow().applyAiAnalysis(
                "통행 불가 위험", "시설물 붕괴 위험", RiskLevel.HIGH, department, "붕괴 위험");

        reportService.submit(reportId, user.getId());

        assertThat(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()))
                .extracting(notification -> notification.getType().name())
                .contains("SUBMITTED", "HIGH_RISK");
        assertThat(notificationRepository.findAllByAdminIdOrderByCreatedAtDesc(firstAdmin.getId()))
                .extracting(notification -> notification.getType().name())
                .containsExactly("HIGH_RISK");
        assertThat(notificationRepository.findAllByAdminIdOrderByCreatedAtDesc(secondAdmin.getId()))
                .extracting(notification -> notification.getType().name())
                .containsExactly("HIGH_RISK");
    }
}
