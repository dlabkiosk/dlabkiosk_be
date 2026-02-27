-- 학번(student_number) 컬럼 추가: 학번으로 학생 인식 지원
ALTER TABLE students ADD COLUMN student_number VARCHAR(30) NULL UNIQUE;

-- 학번 조회 성능을 위한 인덱스
CREATE INDEX idx_student_student_number ON students (student_number);
