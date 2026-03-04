CREATE TABLE seat_leaves (
                             id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                             student_id BIGINT    NOT NULL,
                             store_id   BIGINT    NOT NULL,
                             seat_id    BIGINT    NOT NULL,
                             reason_id  BIGINT    NOT NULL,
                             started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             ended_at   TIMESTAMP NULL,
                             CONSTRAINT fk_seat_leaves_student FOREIGN KEY (student_id) REFERENCES students (id),
                             CONSTRAINT fk_seat_leaves_store FOREIGN KEY (store_id) REFERENCES stores (id),
                             CONSTRAINT fk_seat_leaves_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
                             CONSTRAINT fk_seat_leaves_reason FOREIGN KEY (reason_id) REFERENCES seat_leave_reasons (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_seat_leave_student_started ON seat_leaves (student_id, started_at, ended_at);
