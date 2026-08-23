package com.safewhale.admin_report.controller;

import com.safewhale.admin_report.service.AdminReportService;
import com.safewhale.common.response.ApiResponse;
import com.safewhale.common.response.PageResponse;
import com.safewhale.common.security.SecurityPrincipal;
import com.safewhale.report.domain.ReportStatus;
import com.safewhale.report.domain.RiskLevel;
import com.safewhale.report.dto.ReportResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminReportController {
    private final AdminReportService service;

    @GetMapping("/stats/summary")
    ApiResponse<AdminReportService.StatsResponse> stats() { return ApiResponse.ok(service.stats()); }

    @GetMapping("/departments")
    ApiResponse<List<AdminReportService.DepartmentPendingResponse>> departments() {
        return ApiResponse.ok(service.departments());
    }

    @GetMapping("/reports")
    ApiResponse<PageResponse<ReportResponse>> reports(@RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) Long departmentId, @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        return ApiResponse.ok(service.list(status, departmentId, query, page, size, sort));
    }

    @GetMapping("/reports/{id}")
    ApiResponse<ReportResponse> detail(@PathVariable Long id) { return ApiResponse.ok(service.detail(id)); }

    @GetMapping("/reports/{id}/report-file")
    ApiResponse<FileResponse> reportFile(@PathVariable Long id) { return ApiResponse.ok(new FileResponse(service.reportFile(id))); }

    @PatchMapping("/reports/{id}/status")
    ApiResponse<ReportResponse> status(@PathVariable Long id, @Valid @RequestBody StatusRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.changeStatus(id, principal.id(), request.status()));
    }

    @PatchMapping("/reports/{id}/department")
    ApiResponse<ReportResponse> department(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.reassign(id, principal.id(), request.departmentId()));
    }

    @PatchMapping("/reports/{id}/risk-level")
    ApiResponse<ReportResponse> risk(@PathVariable Long id, @Valid @RequestBody RiskRequest request) {
        return ApiResponse.ok(service.adjustRisk(id, request.riskLevel()));
    }

    @PostMapping("/reports/{id}/resolve")
    ApiResponse<ReportResponse> resolve(@PathVariable Long id, @Valid @RequestBody ResolveRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.resolve(id, principal.id(), request.actionNote()));
    }

    record StatusRequest(@NotNull ReportStatus status) {}
    record DepartmentRequest(@NotNull Long departmentId) {}
    record RiskRequest(@NotNull RiskLevel riskLevel) {}
    record ResolveRequest(@NotBlank String actionNote) {}
    record FileResponse(String url) {}
}
