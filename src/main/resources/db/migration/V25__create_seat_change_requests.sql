CREATE TABLE seat_change_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    current_seat_id BIGINT NULL,
    desired_seat_id_1 BIGINT NOT NULL,
    desired_seat_id_2 BIGINT NULL,
    desired_seat_id_3 BIGINT NULL,
    approved_seat_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_scr_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_scr_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_scr_current_seat FOREIGN KEY (current_seat_id) REFERENCES seats(id) ON DELETE SET NULL,
    CONSTRAINT fk_scr_desired_seat_1 FOREIGN KEY (desired_seat_id_1) REFERENCES seats(id),
    CONSTRAINT fk_scr_desired_seat_2 FOREIGN KEY (desired_seat_id_2) REFERENCES seats(id),
    CONSTRAINT fk_scr_desired_seat_3 FOREIGN KEY (desired_seat_id_3) REFERENCES seats(id),
    CONSTRAINT fk_scr_approved_seat FOREIGN KEY (approved_seat_id) REFERENCES seats(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_scr_student_status ON seat_change_requests(student_id, status);
CREATE INDEX idx_scr_store_status ON seat_change_requests(store_id, status);
