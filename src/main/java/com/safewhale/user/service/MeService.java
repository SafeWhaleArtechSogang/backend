package com.safewhale.user.service;

import com.safewhale.common.exception.BusinessException;
import com.safewhale.common.exception.ErrorCode;
import com.safewhale.notification.repository.NotificationRepository;
import com.safewhale.report.domain.ReportStatus;
import com.safewhale.report.dto.ReportResponse;
import com.safewhale.report.repository.ReportRepository;
import com.safewhale.report.service.ReportViewService;
import com.safewhale.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeService {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;
    private final ReportViewService viewService;

    @Transactional(readOnly = true)
    public ProfileResponse profile(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new ProfileResponse(user.getId(), user.getName(), user.getMajor(), user.getStudentNo());
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> reports(Long userId) {
        return reportRepository.findAllByReporterIdAndStatusNotOrderByCreatedAtDesc(userId, ReportStatus.RECEIVING)
                .stream().map(report -> viewService.toResponse(report, true, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> notifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(item -> new NotificationResponse(item.getId(), item.getReport().getTrackingId(), item.getType().name(),
                        item.getTitle(), item.getMessage(), item.isRead(), item.getCreatedAt())).toList();
    }

    @Transactional
    public void readNotification(Long userId, Long notificationId) {
        var notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markRead();
    }

    public record ProfileResponse(Long id, String name, String major, String studentNo) {}
    public record NotificationResponse(Long id, String trackingId, String type, String title,
                                       String message, boolean read, Instant createdAt) {}
}
