-- 기존 학생 데이터에 임의 전화번호 뒷자리 부여
-- 같은 지점 내 중복되지 않도록 학생 ID 기반으로 생성
UPDATE students SET phone_last4 = LPAD(MOD(id * 1234 + 5678, 10000), 4, '0') WHERE phone_last4 IS NULL;
