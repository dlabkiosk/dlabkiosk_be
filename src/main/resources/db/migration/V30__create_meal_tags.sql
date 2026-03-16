CREATE TABLE meal_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    meal_type VARCHAR(10) NOT NULL,
    meal_date DATE NOT NULL,
    tagged_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_meal_tag_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_meal_tag_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT uk_meal_tag_student_date_type UNIQUE (student_id, meal_date, meal_type),
    INDEX idx_meal_tag_store_date (store_id, meal_date)
);
