package com.safewhale.ai.client;

import com.safewhale.report.domain.RiskLevel;
import org.springframework.stereotype.Component;

@Component
public class MockAiAnalysisClient implements AiAnalysisClient {
    @Override
    public ContentAnalysis analyzeContent(String description, String location) {
        String input = description == null || description.isBlank() ? "현장 위험 요소가 발견되었습니다." : description;
        return new ContentAnalysis("[목 분석] " + abbreviate(input, 170), input, RiskLevel.MEDIUM,
                "FACILITY", "[목 분석] 사진 및 설명 기반 시설 위험 가능성");
    }

    @Override
    public DraftResult draftFromText(String text) {
        return new DraftResult("[목 초안] " + abbreviate(text, 170), text);
    }

    private String abbreviate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
