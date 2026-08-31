package com.safewhale.notification.controller;

import com.safewhale.common.response.ApiResponse;
import com.safewhale.common.security.SecurityPrincipal;
import com.safewhale.notification.service.AdminNotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {
    private final AdminNotificationService service;

    @GetMapping
    ApiResponse<List<AdminNotificationService.NotificationResponse>> notifications(
            @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.notifications(principal.id()));
    }

    @PatchMapping("/{id}/read")
    ApiResponse<Void> read(@PathVariable Long id, @AuthenticationPrincipal SecurityPrincipal principal) {
        service.readNotification(principal.id(), id);
        return ApiResponse.ok();
    }
}
