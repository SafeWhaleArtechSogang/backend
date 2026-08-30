package com.safewhale.ai.client;

import com.safewhale.report.domain.RiskLevel;
import java.util.List;

public interface AiAnalysisClient {
    ContentAnalysis analyzeContent(String description, String location);
    DraftResult draftFromText(String text);

    /**
     * 신고 플로우 확인 질문을 하나씩 만든다.
     *
     * <p>지금까지 받은 질문·답변을 그대로 넘겨 다음 질문의 컨텍스트로 쓴다.
     * 몇 번째 질문인지는 {@code answers.size()} 로 정해지므로 별도 인덱스를 받지 않는다.
     */
    QuestionStep createReportQuestion(ReportContext context, List<Answer> answers);

    /** 신고서 초안 + 위험 등급 + 담당 부서. */
    ReportDraft createReportDraft(ReportContext context, List<Answer> answers);

    /** 사진 한 장을 포함한 신고 1건의 입력. 실제 AI 서버는 사진 없이 판독할 수 없다. */
    record ReportContext(String sessionId, String locationText, String incidentDescription, Photo photo) {}
    record Photo(byte[] bytes, String mimeType) {}

    record ContentAnalysis(String summary, String description, RiskLevel riskLevel,
                           String departmentCode, String detectedHazards) {}
    record DraftResult(String summary, String description) {}
    record Question(String id, String text, List<String> options, boolean allowCustom) {}
    /** 한 번에 질문 하나. {@code introduction} 은 첫 질문에만 채운다. */
    record QuestionStep(String introduction, Question question, int questionIndex, int questionCount,
                        boolean last) {}
    record Answer(String questionId, String question, String answer) {}
    record ReportDraft(String summary, String hazardContent, String improvementSuggestion,
                       RiskLevel riskLevel, String departmentCode, String detectedHazards) {}
}
