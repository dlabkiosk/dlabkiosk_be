CREATE TABLE meal_unapplied_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(10) NOT NULL,
    detected_at DATETIME(6) NOT NULL,
    tag_action VARCHAR(20) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_meal_unapplied_logs_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_meal_unapplied_logs_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT uk_meal_unapplied_logs UNIQUE (student_id, meal_date, meal_type),
    INDEX idx_meal_unapplied_logs_store_date (store_id, meal_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
