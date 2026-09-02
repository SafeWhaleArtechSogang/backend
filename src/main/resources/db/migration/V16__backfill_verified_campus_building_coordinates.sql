-- 서강대학교 공식 캠퍼스맵의 건물명·코드와 대조한 지도 건물 중심 좌표를 반영한다.
-- V10에서 이름만 등록한 건물 중 검증된 항목만 채우며, 확인하지 못한 항목은 NULL로 남겨
-- 프런트의 장소 검색 + 사용자 지도 조정 흐름을 사용한다.
UPDATE buildings
SET lat = CASE code
    WHEN 'K'  THEN 37.5501000
    WHEN 'GA' THEN 37.5520100
    WHEN 'T'  THEN 37.5520500
    WHEN 'MA' THEN 37.5527100
    WHEN 'M'  THEN 37.5521700
    WHEN 'E'  THEN 37.5513400
    WHEN 'CY' THEN 37.5510800
    WHEN 'D'  THEN 37.5520800
    WHEN 'GH' THEN 37.5514000
    WHEN 'GP' THEN 37.5510200
    WHEN 'TE' THEN 37.5504700
    WHEN 'F'  THEN 37.5502200
    WHEN 'RA' THEN 37.5501800
    WHEN 'R'  THEN 37.5498100
    WHEN 'BL' THEN 37.5490600
    WHEN 'SB' THEN 37.5494800
    WHEN 'AR' THEN 37.5499500
    WHEN 'BW' THEN 37.5505900
END,
lng = CASE code
    WHEN 'K'  THEN 126.9400700
    WHEN 'GA' THEN 126.9390200
    WHEN 'T'  THEN 126.9382000
    WHEN 'MA' THEN 126.9392500
    WHEN 'M'  THEN 126.9394500
    WHEN 'E'  THEN 126.9409600
    WHEN 'CY' THEN 126.9423400
    WHEN 'D'  THEN 126.9430900
    WHEN 'GH' THEN 126.9439700
    WHEN 'GP' THEN 126.9431600
    WHEN 'TE' THEN 126.9434600
    WHEN 'F'  THEN 126.9426500
    WHEN 'RA' THEN 126.9421700
    WHEN 'R'  THEN 126.9410700
    WHEN 'BL' THEN 126.9401300
    WHEN 'SB' THEN 126.9393700
    WHEN 'AR' THEN 126.9387400
    WHEN 'BW' THEN 126.9390700
END,
updated_at = CURRENT_TIMESTAMP
WHERE code IN ('K', 'GA', 'T', 'MA', 'M', 'E', 'CY', 'D', 'GH', 'GP', 'TE', 'F', 'RA', 'R', 'BL', 'SB', 'AR', 'BW');

-- 기존 V2의 하비에르관(X) 좌표는 공식 근거를 확보하지 못했다.
-- 잘못된 고정 위치 대신 사용자가 장소 검색 결과를 확인·조정하게 한다.
UPDATE buildings
SET lat = NULL,
    lng = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'X'
  AND name = '하비에르관';
