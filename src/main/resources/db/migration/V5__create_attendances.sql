CREATE TABLE attendances (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT      NOT NULL,
    store_id     BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL,
    check_in_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_at TIMESTAMP   NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendances_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_attendances_store   FOREIGN KEY (store_id)   REFERENCES stores (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
