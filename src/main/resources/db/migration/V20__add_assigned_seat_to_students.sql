ALTER TABLE students ADD COLUMN assigned_seat_id BIGINT NULL;

ALTER TABLE students ADD CONSTRAINT fk_students_assigned_seat
    FOREIGN KEY (assigned_seat_id) REFERENCES seats (id) ON DELETE SET NULL;
