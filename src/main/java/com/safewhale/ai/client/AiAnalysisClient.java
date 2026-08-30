package com.safewhale.ai.client;

import com.safewhale.report.domain.RiskLevel;
import java.util.List;

public interface AiAnalysisClient {
    ContentAnalysis analyzeContent(String description, String location);
    DraftResult draftFromText(String text);

    /** 신고 플로우 확인 질문. 사진 판독이 필요해 컨텍스트 전체를 받는다. */
    QuestionSet createReportQuestions(ReportContext context);

    /** 신고서 초안 + 위험 등급 + 담당 부서. */
    ReportDraft createReportDraft(ReportContext context, List<Answer> answers);

    /** 사진 한 장을 포함한 신고 1건의 입력. 실제 AI 서버는 사진 없이 판독할 수 없다. */
    record ReportContext(String sessionId, String locationText, String incidentDescription, Photo photo) {}
    record Photo(byte[] bytes, String mimeType) {}

    record ContentAnalysis(String summary, String description, RiskLevel riskLevel,
                           String departmentCode, String detectedHazards) {}
    record DraftResult(String summary, String description) {}
    record Question(String id, String text, List<String> options, boolean allowCustom) {}
    record QuestionSet(String introduction, List<Question> questions) {}
    record Answer(String questionId, String question, String answer) {}
    record ReportDraft(String summary, String hazardContent, String improvementSuggestion,
                       RiskLevel riskLevel, String departmentCode, String detectedHazards) {}
}
