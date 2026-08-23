package com.safewhale.ai.controller;

import com.safewhale.ai.client.AiAnalysisClient;
import com.safewhale.ai.service.AiAnalysisService;
import com.safewhale.common.response.ApiResponse;
import com.safewhale.common.security.SecurityPrincipal;
import com.safewhale.report.dto.ReportResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiAnalysisService service;

    @PostMapping("/analyze-location")
    ApiResponse<List<AiAnalysisService.LocationCandidate>> location(@Valid @RequestBody LocationRequest request) {
        return ApiResponse.ok(service.analyzeLocation(request.lat(), request.lng()));
    }

    @PostMapping("/analyze-content")
    ApiResponse<ReportResponse> content(@Valid @RequestBody ContentRequest request,
                                        @AuthenticationPrincipal SecurityPrincipal principal) {
        return ApiResponse.ok(service.analyzeContent(request.reportId(), principal.id(), request.description()));
    }

    @PostMapping("/draft-from-text")
    ApiResponse<AiAnalysisClient.DraftResult> draft(@Valid @RequestBody DraftRequest request) {
        return ApiResponse.ok(service.draftFromText(request.text()));
    }

    record LocationRequest(@NotNull BigDecimal lat, @NotNull BigDecimal lng) {}
    record ContentRequest(@NotNull Long reportId, String description) {}
    record DraftRequest(@NotBlank String text) {}
}
