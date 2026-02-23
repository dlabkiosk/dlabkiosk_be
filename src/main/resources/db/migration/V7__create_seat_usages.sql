CREATE TABLE seat_usages
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_id    BIGINT      NOT NULL,
    student_id BIGINT      NOT NULL,
    store_id   BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL,
    started_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at   TIMESTAMP   NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_usages_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
    CONSTRAINT fk_seat_usages_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_seat_usages_store FOREIGN KEY (store_id) REFERENCES stores (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
