-- 공식 명칭은 확인됐지만 좌표가 아직 검수되지 않은 건물도 선택지에 제공할 수 있게 한다.
-- 좌표가 없는 경우 신고 화면에서 장소 검색 후 사용자가 마커를 확정한다.
ALTER TABLE buildings
    ALTER COLUMN lat DROP NOT NULL,
    ALTER COLUMN lng DROP NOT NULL;
