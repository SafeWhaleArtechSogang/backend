-- V2의 정하상관(J관) 초기 좌표가 실제 건물 중심과 달라 지도 선택 시 인접 건물로 표시됐다.
-- WGS84 기준 건물 중심 좌표로 보정한다.
UPDATE buildings
SET lat = 37.5502800,
    lng = 126.9429900,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'J'
  AND name = '정하상관';
