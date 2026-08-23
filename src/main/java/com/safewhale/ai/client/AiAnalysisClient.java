package com.safewhale.ai.client;

import com.safewhale.report.domain.RiskLevel;

public interface AiAnalysisClient {
    ContentAnalysis analyzeContent(String description, String location);
    DraftResult draftFromText(String text);

    record ContentAnalysis(String summary, String description, RiskLevel riskLevel,
                           String departmentCode, String detectedHazards) {}
    record DraftResult(String summary, String description) {}
}
