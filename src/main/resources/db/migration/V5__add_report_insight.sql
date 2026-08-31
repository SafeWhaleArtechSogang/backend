-- ④ 위험도 재산출의 판단 근거. 등급(risk_level)만으로는 왜 그렇게 매겨졌는지 알 수 없다.
ALTER TABLE reports ADD COLUMN risk_level_rationale TEXT;
ALTER TABLE reports ADD COLUMN risk_level_changed BOOLEAN;

-- ⑤ 요약·인사이트. 담당자 내부용이며 제출 후 비동기로 생성되므로 없을 수 있다.
CREATE TABLE report_insights (
    id                 BIGSERIAL PRIMARY KEY,
    report_id          BIGINT      NOT NULL UNIQUE REFERENCES reports (id) ON DELETE CASCADE,
    summary            TEXT        NOT NULL,
    keywords           TEXT,
    recurring          BOOLEAN     NOT NULL DEFAULT FALSE,
    insight            TEXT        NOT NULL,
    recommended_priority VARCHAR(10) NOT NULL,
    related_report_ids TEXT,
    similar_case_count INTEGER     NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
