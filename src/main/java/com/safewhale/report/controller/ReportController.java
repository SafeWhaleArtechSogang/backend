package com.safewhale.report.controller;

import com.safewhale.common.response.ApiResponse;
import com.safewhale.common.security.SecurityPrincipal;
import com.safewhale.photo.service.PhotoService;
import com.safewhale.report.dto.ReportResponse;
import com.safewhale.report.service.ReportService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final PhotoService photoService;

    @PostMapping("/draft")
    ApiResponse<ReportResponse> draft(@AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(reportService.createDraft(principal.id()));
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ReportResponse> photos(@PathVariable Long id, @RequestPart("photos") List<MultipartFile> photos,
                                       @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(photoService.upload(id, principal.id(), photos));
    }

    @PatchMapping("/{id}/location")
    ApiResponse<ReportResponse> location(@PathVariable Long id, @RequestBody ReportService.LocationRequest request,
                                         @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(reportService.updateLocation(id, principal.id(), request));
    }

    @PatchMapping("/{id}")
    ApiResponse<ReportResponse> update(@PathVariable Long id, @RequestBody ReportService.UpdateRequest request,
                                       @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(reportService.updateDraft(id, principal.id(), request));
    }

    @GetMapping("/{id}/duplicates")
    ApiResponse<List<ReportResponse>> duplicates(@PathVariable Long id, @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(reportService.duplicates(id, principal.id()));
    }

    @PostMapping("/{id}/report-file")
    ApiResponse<ReportResponse> reportFile(@PathVariable Long id, @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(reportService.generateReportFile(id, principal.id()));
    }

    @PostMapping("/{id}/submit")
    ApiResponse<ReportResponse> submit(@PathVariable Long id, @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(reportService.submit(id, principal.id()));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal SecurityPrincipal principal) {
        reportService.deleteDraft(id, principal.id());
        return ApiResponse.ok();
    }

    @GetMapping("/map")
    ApiResponse<List<ReportResponse>> map(@RequestParam(defaultValue = "all") String filter,
                                          @RequestParam(required = false) String bbox,
                                          @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(reportService.map(filter, principal == null ? null : principal.id(), bbox));
    }

    @GetMapping("/{trackingId}")
    ApiResponse<ReportResponse> detail(@PathVariable String trackingId) {
        return ApiResponse.ok(reportService.findByTrackingId(trackingId));
    }
}
