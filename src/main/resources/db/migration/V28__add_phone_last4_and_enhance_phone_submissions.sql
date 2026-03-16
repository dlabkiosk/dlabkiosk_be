-- Student: 전화번호 뒷자리 4자리 추가 (DSA에서 동기화)
ALTER TABLE students ADD COLUMN phone_last4 VARCHAR(4) NULL AFTER student_number;
CREATE INDEX idx_student_phone_last4_store ON students (phone_last4, store_id);

-- PhoneSubmission: 신청 유형 + 기간 설정
ALTER TABLE phone_submissions ADD COLUMN submission_type VARCHAR(20) NOT NULL DEFAULT 'DAILY' AFTER submitted_at;
ALTER TABLE phone_submissions ADD COLUMN start_date DATE NOT NULL DEFAULT (CURRENT_DATE) AFTER submission_type;
ALTER TABLE phone_submissions ADD COLUMN end_date DATE NULL AFTER start_date;

-- 기존 데이터: DAILY 타입, start_date = submitted_at 날짜
UPDATE phone_submissions SET start_date = DATE(submitted_at), end_date = DATE(submitted_at) WHERE submission_type = 'DAILY';
