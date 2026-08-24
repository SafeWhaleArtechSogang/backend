package com.safewhale.ai.client;

import com.safewhale.report.domain.RiskLevel;
import java.util.List;

public interface AiAnalysisClient {
    ContentAnalysis analyzeContent(String description, String location);
    DraftResult draftFromText(String text);
    QuestionSet createReportQuestions(String incidentDescription);
    ReportDraft createReportDraft(String locationDescription, String incidentDescription, List<Answer> answers);

    record ContentAnalysis(String summary, String description, RiskLevel riskLevel,
                           String departmentCode, String detectedHazards) {}
    record DraftResult(String summary, String description) {}
    record Question(String id, String text, List<String> options, boolean allowCustom) {}
    record QuestionSet(String introduction, List<Question> questions) {}
    record Answer(String questionId, String question, String answer) {}
    record ReportDraft(String summary, String hazardContent, String improvementSuggestion) {}
}
