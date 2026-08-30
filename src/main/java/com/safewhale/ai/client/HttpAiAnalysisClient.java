package com.safewhale.ai.client;

import com.safewhale.ai.config.AiServerProperties;
import com.safewhale.common.exception.BusinessException;
import com.safewhale.common.exception.ErrorCode;
import com.safewhale.report.domain.RiskLevel;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ai-server(Gemini) 를 실제로 호출하는 {@link AiAnalysisClient} 구현.
 *
 * <p>신고 플로우 한 건은 다음 순서로 AI 서버를 탄다.
 * <pre>
 *   질문 단계 : ① /v1/vision/analyze → ② /v1/questions/next × N
 *   초안 단계 : (①은 캐시 재사용) → ④ /v1/drafts/generate → ③ /v1/departments/classify
 * </pre>
 *
 * <p>ai-server 의 ②는 한 번에 질문 하나만 만드는 대화형 설계지만, 프런트는 질문 3개를
 * 한꺼번에 받아 화면에서 하나씩 보여준다. 그래서 여기서 N 번 순차 호출하며 앞서 만든
 * 질문을 "아직 답변 없음" 상태로 이력에 넣어 같은 질문이 반복되지 않게 한다.
 * (프런트를 순차 호출로 바꾸면 직전 답변까지 반영돼 질문 품질이 더 올라간다.)
 */
@Component
@ConditionalOnProperty(name = "app.external.ai", havingValue = "http")
public class HttpAiAnalysisClient implements AiAnalysisClient {
    private static final Logger log = LoggerFactory.getLogger(HttpAiAnalysisClient.class);

    /** 프런트가 "질문 세 가지만" 구절을 굵게 강조하므로 문구를 유지한다. */
    private static final String QUESTION_INTRO = "신고서를 작성하기 위한 질문 세 가지만 더 물어볼게요.";
    /** 답변이 아직 없어도 "이미 물어본 질문"이라는 사실은 알려야 같은 질문이 반복되지 않는다. */
    private static final String UNANSWERED = "(아직 답변 전. 이미 제시한 질문이므로 같은 주제를 다시 묻지 말 것)";

    private final AiServerClient aiServer;
    private final AiVisionCache visionCache;
    private final AiServerProperties properties;

    /** 사진 없이 텍스트만 다루는 레거시 엔드포인트용. AI 서버에 대응 경로가 없다. */
    private final MockAiAnalysisClient textFallback = new MockAiAnalysisClient();

    public HttpAiAnalysisClient(AiServerClient aiServer, AiVisionCache visionCache, AiServerProperties properties) {
        this.aiServer = aiServer;
        this.visionCache = visionCache;
        this.properties = properties;
    }

    /**
     * {@code POST /api/v1/ai/analyze-content} 용. 사진 없이 텍스트만 받는 계약이라
     * ai-server 의 판독 파이프라인에 태울 수 없어 목 응답을 그대로 쓴다.
     * 신고 플로우(질문·초안)는 이 경로를 타지 않는다.
     */
    @Override
    public ContentAnalysis analyzeContent(String description, String location) {
        log.warn("analyzeContent 는 AI 서버 연동 대상이 아니다 (사진 없는 텍스트 전용 계약) — 목 응답을 반환한다.");
        return textFallback.analyzeContent(description, location);
    }

    /** {@code POST /api/v1/ai/draft-from-text} 용. 위와 같은 이유로 목 응답을 쓴다. */
    @Override
    public DraftResult draftFromText(String text) {
        log.warn("draftFromText 는 AI 서버 연동 대상이 아니다 — 목 응답을 반환한다.");
        return textFallback.draftFromText(text);
    }

    @Override
    public QuestionSet createReportQuestions(ReportContext context) {
        AiServerClient.VisionResponse vision = analyzeVision(context);
        int count = properties.questionCount();

        List<Question> questions = new ArrayList<>(count);
        StringJoiner history = new StringJoiner("\n");
        for (int index = 1; index <= count; index++) {
            AiServerClient.QuestionResponse response = aiServer.nextQuestion(new AiServerClient.QuestionRequest(
                    context.sessionId(), context.locationText(), context.incidentDescription(), vision.toPayload(),
                    history.length() == 0 ? null : history.toString(), index, count));

            List<String> options = response.options() == null ? List.of()
                    : response.options().stream().map(AiServerClient.OptionPayload::label).toList();
            questions.add(new Question("q" + index, response.text(), options, true));

            history.add("Q%d. %s".formatted(index, response.text()));
            history.add("A%d. %s".formatted(index, UNANSWERED));
        }
        log.debug("AI 확인 질문 {}개 생성 완료 session={}", questions.size(), context.sessionId());
        return new QuestionSet(QUESTION_INTRO, questions);
    }

    @Override
    public ReportDraft createReportDraft(ReportContext context, List<Answer> answers) {
        AiServerClient.VisionResponse vision = analyzeVision(context);

        AiServerClient.DraftResponse draft = aiServer.generateDraft(new AiServerClient.DraftRequest(
                context.sessionId(), "generate", context.locationText(), context.incidentDescription(),
                vision.toPayload(), vision.riskLevel(), formatQaHistory(answers)));

        AiServerClient.DepartmentResponse department = aiServer.classifyDepartment(
                new AiServerClient.DepartmentRequest(context.sessionId(), context.locationText(), vision.toPayload()));

        log.debug("AI 초안 생성 완료 session={} risk={} dept={}",
                context.sessionId(), draft.riskLevelFinal(), department.departmentCode());

        return new ReportDraft(
                draft.title(),
                hazardContent(draft),
                improvementSuggestion(draft),
                toRiskLevel(draft.riskLevelFinal()),
                department.departmentCode(),
                vision.detectedHazard());
    }

    private AiServerClient.VisionResponse analyzeVision(ReportContext context) {
        AiServerClient.VisionResponse cached = visionCache.get(context.sessionId());
        if (cached != null) return cached;

        Photo photo = context.photo();
        if (photo == null || photo.bytes() == null || photo.bytes().length == 0) {
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR, "사진이 있어야 AI 판독을 할 수 있습니다.");
        }
        AiServerClient.VisionResponse vision = aiServer.analyzeVision(new AiServerClient.VisionRequest(
                context.sessionId(), context.locationText(), context.incidentDescription(),
                new AiServerClient.PhotoPayload(Base64.getEncoder().encodeToString(photo.bytes()), photo.mimeType())));
        visionCache.put(context.sessionId(), vision);
        return vision;
    }

    private String formatQaHistory(List<Answer> answers) {
        if (answers == null || answers.isEmpty()) return null;
        StringJoiner joiner = new StringJoiner("\n");
        int index = 1;
        for (Answer answer : answers) {
            joiner.add("Q%d. %s".formatted(index, answer.question()));
            joiner.add("A%d. %s".formatted(index, answer.answer()));
            index++;
        }
        return joiner.toString();
    }

    /** 신고서의 "안전·보건 유해/위험/시설/장소 내용" 칸. 화면에서 그대로 수정할 수 있어야 해 항목별로 편다. */
    private String hazardContent(AiServerClient.DraftResponse draft) {
        StringJoiner joiner = new StringJoiner("\n");
        appendItem(joiner, "위험 요소", draft.hazard());
        appendItem(joiner, "현장 상태", draft.photoAnalysis());
        appendItem(joiner, "위험성", draft.risk());
        appendItem(joiner, "노출 대상", draft.exposedTarget());
        return joiner.toString();
    }

    /** 신고서의 "개선 제안 사항" 칸. */
    private String improvementSuggestion(AiServerClient.DraftResponse draft) {
        StringJoiner joiner = new StringJoiner("\n");
        appendItem(joiner, "긴급 조치", draft.actionUrgent());
        appendItem(joiner, "근본 대책", draft.actionFundamental());
        appendItem(joiner, "기타 의견", draft.otherOpinion());
        return joiner.toString();
    }

    private void appendItem(StringJoiner joiner, String label, String value) {
        if (value != null && !value.isBlank()) joiner.add("· " + label + ": " + value.trim());
    }

    /**
     * AI 서버는 low/medium/high/critical 4단계를, 백엔드는 LOW/MEDIUM/HIGH 3단계를 쓴다.
     * critical 은 가장 높은 등급인 HIGH 로 접는다.
     */
    private RiskLevel toRiskLevel(String value) {
        return switch (value == null ? "" : value.toLowerCase(Locale.ROOT)) {
            case "low" -> RiskLevel.LOW;
            case "high", "critical" -> RiskLevel.HIGH;
            default -> RiskLevel.MEDIUM;
        };
    }
}
