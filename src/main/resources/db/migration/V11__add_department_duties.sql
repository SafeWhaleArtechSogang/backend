-- C-1 부서 업무범위 검색(POST /internal/v1/rag/duty-search)의 코퍼스.
-- C-2 부서 확인(POST /internal/v1/departments/resolve)이 쓰는 contact·active 컬럼도 함께 추가한다.
--
-- 계약: AI연동_API_명세서.md §4.2 / §5.2
-- 후보 1건 = 부서가 아니라 "업무범위 문장 1개"다. 같은 부서가 여러 번 나올 수 있다.

ALTER TABLE departments
    ADD COLUMN contact VARCHAR(50),
    ADD COLUMN active  BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE departments SET contact = '02-705-8114' WHERE code = 'FACILITY';
UPDATE departments SET contact = '02-705-8998' WHERE code = 'SAFETY_CENTER';
UPDATE departments SET contact = '02-705-8032' WHERE code = 'GENERAL_AFFAIRS';

CREATE TABLE department_duties (
    id            BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    duty_text     TEXT   NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_department_duties_department ON department_duties (department_id);

-- 코퍼스 버전. 업무범위 문장을 고칠 때마다 올린다. AI 서버가 응답에 그대로 실어
-- 어느 코퍼스로 판정했는지 추적한다.
CREATE TABLE department_duty_corpus (
    id      SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    version INTEGER  NOT NULL,
    CONSTRAINT ck_duty_corpus_single_row CHECK (id = 1)
);
INSERT INTO department_duty_corpus (id, version) VALUES (1, 1);

-- 업무범위 15문장. ai-server/app/backends/fixtures/departments.json 과 문장이 동일해야
-- stub 모드와 http 모드의 판정이 갈리지 않는다.
INSERT INTO department_duties (department_id, duty_text)
SELECT d.id, t.duty_text
FROM departments d
JOIN (VALUES
    ('FACILITY', '건물 바닥·계단·복도의 마감재 파손, 타일 들뜸·깨짐, 미끄럼 방지 처리 및 보수를 담당한다.'),
    ('FACILITY', '출입문, 창호, 난간, 손잡이 등 건축 부속물의 고정 불량·파손 보수를 담당한다.'),
    ('FACILITY', '누수, 배관 파열, 천장재 탈락, 벽체 균열 등 건축 구조물 하자의 점검과 보수를 담당한다.'),
    ('FACILITY', '옥내외 조명, 배전반, 콘센트 등 전기설비의 파손·노출·불량에 대한 점검과 교체를 담당한다.'),
    ('FACILITY', '냉난방기, 승강기, 급배수 설비 등 기계설비의 고장 접수와 정비를 담당한다.'),
    ('SAFETY_CENTER', '실험실·연구실의 화학물질 보관 불량, 유해가스 누출, 실험 장비 안전장치 미비를 담당한다.'),
    ('SAFETY_CENTER', '소화기·소화전·비상구·피난 유도등 등 소방시설의 미비와 비상통로 적치물을 담당한다.'),
    ('SAFETY_CENTER', '고압가스, 유해화학물질, 방사선 발생장치 등 위험물 취급 구역의 안전관리를 담당한다.'),
    ('SAFETY_CENTER', '작업 현장의 추락·끼임·감전 위험, 보호구 미착용 등 산업안전보건 사항을 담당한다.'),
    ('SAFETY_CENTER', '캠퍼스 내 안전사고 발생 시 조사와 재발방지 대책 수립을 담당한다.'),
    ('GENERAL_AFFAIRS', '강의실·사무실 집기와 비품의 파손, 노후 교체, 배치 변경을 담당한다.'),
    ('GENERAL_AFFAIRS', '청소·미화, 폐기물 적치, 방치된 대형 폐기물의 수거를 담당한다.'),
    ('GENERAL_AFFAIRS', '주차 질서, 캠퍼스 차량 통행, 교내 도로 표지와 차단 시설의 운영을 담당한다.'),
    ('GENERAL_AFFAIRS', '외부 용역·경비 인력의 배치와 교내 보안 순찰 운영을 담당한다.'),
    ('GENERAL_AFFAIRS', '교내 게시물, 현수막, 안내 표지판의 설치와 정비를 담당한다.')
) AS t(code, duty_text) ON t.code = d.code;
