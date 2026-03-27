ALTER TABLE students ADD COLUMN phone VARCHAR(20) NULL AFTER phone_last4;
CREATE INDEX idx_student_phone_store ON students (phone, store_id);
