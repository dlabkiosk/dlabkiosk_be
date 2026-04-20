-- 비활성 학생이 RFID를 계속 점유하면 RFID 재부여 시 UNIQUE 제약 위반 또는
-- 좀비 row 재사용으로 과거 이력이 신규 학생에게 오염됨.
-- 탈퇴 시 RFID를 NULL로 비우기 위해 컬럼을 NULL 허용으로 변경하고
-- 기존 비활성 학생의 RFID는 일괄 NULL 처리.

UPDATE students SET rfid_uid = NULL WHERE is_active = FALSE;

ALTER TABLE students MODIFY rfid_uid VARCHAR(50) NULL;
