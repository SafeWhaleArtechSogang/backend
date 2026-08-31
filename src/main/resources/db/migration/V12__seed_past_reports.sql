-- C-5 과거사례 검색(POST /internal/v1/cases/search)이 돌려줄 과거 신고 8건.
-- 원본은 ai-server/app/backends/fixtures/cases.json 이며 컬럼이 1:1 로 대응한다.
--
-- 계약: 2026-08-31_AI_과거사례_인사이트_C5계약.md §5
--
-- 날짜를 절대값으로 박지 않는다. days_ago 상대값으로 넣어야 시간이 지나도
-- "최근 30일 K건" 이 마르지 않는다. tracking_id 도 그 날짜에서 파생시킨다
-- (ai-server stub 의 R-YYYY-MMDD 규칙과 동일).

-- reports.reporter_id 가 NOT NULL 이라 시드 전용 시스템 유저가 하나 필요하다.
INSERT INTO users (provider, provider_id, name)
SELECT 'GOOGLE', 'system-seed', '시스템(과거사례 시드)'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE provider_id = 'system-seed');

WITH seed(days_ago, building_name, floor, status, risk_level, hazard, photo_analysis,
          risk, action_fundamental, department_code) AS (
    VALUES
    (8,   '김대건관',   '2',  'RESOLVED',  'MEDIUM',
     '김대건관 2층 복도 바닥 타일 들뜸 재발',
     '이전 보수 구간 인접부 타일 3매가 들떠 발 디딤 시 유동이 발생함.',
     '들뜬 타일이 밟히면서 미끄러져 전도 사고로 이어질 수 있음.',
     '접착 불량 원인인 바닥 습기를 조사한 뒤 방습 처리 후 재시공함.', 'FACILITY'),
    (19,  '김대건관',   '2',  'REVIEWING', 'HIGH',
     '김대건관 2층 계단 진입부 바닥 마감재 파손',
     '계단 진입부 마감재가 폭 30cm 가량 떨어져 나가 하부 콘크리트가 노출됨.',
     '계단 진입 직전 발이 걸려 계단으로 넘어질 수 있음.',
     '', 'FACILITY'),
    (26,  '김대건관',   'B1', 'RECEIVED',  'MEDIUM',
     '김대건관 지하 주차장 조명 소등 구간 발생',
     '지하 1층 주차 구역 조명 4등 중 3등이 소등되어 시야 확보가 어려움.',
     '보행자 인지 지연으로 차량과의 접촉 사고가 발생할 수 있음.',
     '', 'FACILITY'),
    (47,  '김대건관',   '2',  'RESOLVED',  'HIGH',
     '김대건관 2층 복도 소화전 앞 적치물',
     '소화전 전면에 사물함과 폐지 묶음이 쌓여 개폐가 불가한 상태임.',
     '화재 발생 시 소화전 사용이 지연될 수 있음.',
     '층별 적치 금지 구역을 표시하고 월 1회 소방시설 접근성 점검을 정례화함.', 'SAFETY_CENTER'),
    (62,  '하비에르관', '4',  'RESOLVED',  'HIGH',
     '하비에르관 실험실 복도 소화기 적치물 가림',
     '소화기함 앞에 실험 폐기물 상자가 적치되어 접근이 불가한 상태임.',
     '화재 발생 시 초기 진화 장비 접근이 지연될 수 있음.',
     '폐기물 임시 보관 구역을 지정하고 월 1회 소방시설 접근성 점검을 정례화함.', 'SAFETY_CENTER'),
    (88,  '정하상관',   '3',  'RESOLVED',  'HIGH',
     '정하상관 계단 난간 고정 불량',
     '3층 계단참 난간 지주 하부 앵커가 풀려 좌우로 유동함.',
     '난간에 체중이 실릴 경우 이탈해 추락 사고로 이어질 수 있음.',
     '앵커볼트를 재시공하고 동일 시공 구간 난간 전수 점검을 실시함.', 'FACILITY'),
    (141, '하비에르관', '1',  'RESOLVED',  'MEDIUM',
     '하비에르관 1층 출입구 바닥 타일 파손 (모서리 돌출)',
     '출입구 매트 앞 바닥 타일 2매가 깨져 모서리가 약 1cm 돌출된 상태임.',
     '돌출된 타일 모서리에 보행자가 걸려 전도될 수 있음.',
     '파손 타일을 철거하고 동일 규격 타일로 재시공하며, 인접 타일 들뜸 여부를 함께 점검함.', 'FACILITY'),
    (205, '정하상관',   '1',  'RESOLVED',  'LOW',
     '정하상관 앞 대형 폐기물 장기 방치',
     '책상·의자 등 폐기 집기가 보도 폭의 절반을 점유한 채 2주 이상 방치됨.',
     '보행 동선이 좁아져 통행 중 충돌·전도가 발생할 수 있음.',
     '대형 폐기물 수거 주기를 단축하고 임시 적치 구역을 별도 지정함.', 'GENERAL_AFFAIRS')
),
resolved AS (
    SELECT s.*,
           (now() - make_interval(days => s.days_ago)) AS submitted_at,
           b.id AS building_id,
           d.id AS department_id,
           (SELECT id FROM users WHERE provider_id = 'system-seed') AS reporter_id
    FROM seed s
    LEFT JOIN buildings   b ON b.name = s.building_name
    LEFT JOIN departments d ON d.code = s.department_code
),
inserted AS (
    INSERT INTO reports (tracking_id, summary, description, status, risk_level, risk_level_source,
                         department_id, reporter_id, reporter_type, building_id, building_name_snapshot,
                         is_indoor, floor, detected_hazards, submitted_at, created_at, updated_at)
    SELECT 'R-' || to_char(r.submitted_at, 'YYYY-MMDD'),
           r.hazard,
           r.photo_analysis || ' ' || r.risk,
           r.status,
           r.risk_level,
           'AI',
           r.department_id,
           r.reporter_id,
           'ANONYMOUS',
           r.building_id,
           r.building_name,
           TRUE,
           r.floor,
           r.hazard,
           r.submitted_at,
           r.submitted_at,
           r.submitted_at
    FROM resolved r
    RETURNING id, tracking_id, status, submitted_at
)
-- 근본 조치는 reports 가 아니라 활동 로그에 있다. AdminReportService.resolve() 가
-- 실제 운영에서 저장하는 자리와 같은 곳을 쓴다 (C-5 가 여기서 읽는다).
INSERT INTO report_activity_logs (report_id, activity_type, from_status, to_status,
                                  actor_type, actor_id, action_note, created_at)
SELECT i.id, 'RESOLVE', 'REVIEWING', 'RESOLVED', 'SYSTEM', NULL, r.action_fundamental, i.submitted_at
FROM inserted i
JOIN resolved r ON 'R-' || to_char(r.submitted_at, 'YYYY-MMDD') = i.tracking_id
WHERE i.status = 'RESOLVED' AND r.action_fundamental <> '';
