-- pgvector 준비. **이번 단계에서는 쓰지 않는다.**
--
-- C-5 1단계는 "동일 건물 우선 → 최근순" 이고 score 는 항상 null 이다.
-- 2단계에서 ORDER BY 를 embedding 거리로 바꾸고 score 를 채우면 되는데,
-- 그때 응답 계약은 그대로다 (2026-08-31_AI_과거사례_인사이트_C5계약.md §6).
--
-- 지금 확장을 켜 두는 이유는 순전히 이전 비용 때문이다.
-- postgres:16-alpine(musl) → pgvector/pgvector:pg16(glibc) 이미지 교체는
-- 데이터가 적을 때 해야 REINDEX 가 순식간에 끝난다. 쌓인 뒤에는 다운타임이 된다.
--
-- reports 테이블은 건드리지 않는다. 임베딩은 별도 테이블에 둔다 —
-- 재색인·모델 교체 때 신고 본문이 영향을 받지 않아야 한다.

CREATE EXTENSION IF NOT EXISTS vector;

-- 차원 수는 Gemini text-embedding 계열 기본값(768)에 맞춘다.
-- 모델을 바꿔 차원이 달라지면 이 테이블만 새로 만들면 된다.
CREATE TABLE report_embeddings (
    report_id   BIGINT PRIMARY KEY REFERENCES reports(id) ON DELETE CASCADE,
    model       VARCHAR(64)  NOT NULL,
    embedding   vector(768)  NOT NULL,
    source_text TEXT         NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스는 행이 쌓인 뒤에 만든다. 빈 테이블에 ivfflat 을 만들면 클러스터가
-- 학습되지 않아 오히려 리콜이 나빠진다. 지금은 시퀀셜 스캔으로 충분하다.
COMMENT ON TABLE report_embeddings IS
    '신고 본문 임베딩. C-5 2단계 벡터 정렬용. 현재 미사용 — 행이 쌓이면 ivfflat/hnsw 인덱스를 추가할 것.';
