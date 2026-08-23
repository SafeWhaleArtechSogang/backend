package com.safewhale.ai.service;

import com.safewhale.ai.client.AiAnalysisClient;
import com.safewhale.building.repository.BuildingRepository;
import com.safewhale.common.exception.BusinessException;
import com.safewhale.common.exception.ErrorCode;
import com.safewhale.department.repository.DepartmentRepository;
import com.safewhale.report.dto.ReportResponse;
import com.safewhale.report.service.ReportService;
import com.safewhale.report.service.ReportViewService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {
    private final AiAnalysisClient client;
    private final ReportService reportService;
    private final ReportViewService viewService;
    private final DepartmentRepository departmentRepository;
    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public List<LocationCandidate> analyzeLocation(BigDecimal lat, BigDecimal lng) {
        return buildingRepository.findAll().stream().limit(5)
                .map(building -> new LocationCandidate(building.getId(), building.getName(), building.getLat(), building.getLng()))
                .toList();
    }

    @Transactional
    public ReportResponse analyzeContent(Long reportId, Long userId, String description) {
        var report = reportService.getOwnedDraft(reportId, userId);
        String location = report.getBuildingNameSnapshot() == null ? "위치 미정" : report.getBuildingNameSnapshot();
        var result = client.analyzeContent(description, location);
        var department = departmentRepository.findByCode(result.departmentCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
        report.applyAiAnalysis(result.summary(), result.description(), result.riskLevel(), department, result.detectedHazards());
        return viewService.toResponse(report, false, false);
    }

    public AiAnalysisClient.DraftResult draftFromText(String text) {
        return client.draftFromText(text);
    }

    public record LocationCandidate(Long buildingId, String name, BigDecimal lat, BigDecimal lng) {}
}
