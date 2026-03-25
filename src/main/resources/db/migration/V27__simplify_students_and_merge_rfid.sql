-- RFID/QR 통합: qr_uuid 값을 rfid_uid가 없는 학생에게 이관
UPDATE students
SET rfid_uid = qr_uuid
WHERE rfid_uid IS NULL AND qr_uuid IS NOT NULL;

-- 불필요 컬럼 삭제: phone, grade, class_name, qr_uuid
ALTER TABLE students DROP COLUMN phone;
ALTER TABLE students DROP COLUMN grade;
ALTER TABLE students DROP COLUMN class_name;
ALTER TABLE students DROP COLUMN qr_uuid;

-- rfid_uid NOT NULL 제약 추가
ALTER TABLE students MODIFY COLUMN rfid_uid VARCHAR(50) NOT NULL;
