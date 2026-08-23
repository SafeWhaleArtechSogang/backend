package com.safewhale.user.controller;

import com.safewhale.common.response.ApiResponse;
import com.safewhale.common.security.SecurityPrincipal;
import com.safewhale.report.dto.ReportResponse;
import com.safewhale.user.service.MeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {
    private final MeService service;

    @GetMapping("/profile")
    ApiResponse<MeService.ProfileResponse> profile(@AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.profile(principal.id()));
    }

    @GetMapping("/reports")
    ApiResponse<List<ReportResponse>> reports(@AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.reports(principal.id()));
    }

    @GetMapping("/notifications")
    ApiResponse<List<MeService.NotificationResponse>> notifications(@AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.notifications(principal.id()));
    }

    @PatchMapping("/notifications/{id}/read")
    ApiResponse<Void> read(@PathVariable Long id, @AuthenticationPrincipal SecurityPrincipal principal) {
        service.readNotification(principal.id(), id);
        return ApiResponse.ok();
    }
}
