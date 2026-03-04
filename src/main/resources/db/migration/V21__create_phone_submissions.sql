CREATE TABLE phone_submissions
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT    NOT NULL,
    store_id     BIGINT    NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_phone_submissions_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_phone_submissions_store FOREIGN KEY (store_id) REFERENCES stores (id),
    INDEX idx_phone_sub_store_date (store_id, submitted_at),
    INDEX idx_phone_sub_student_date (student_id, submitted_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
