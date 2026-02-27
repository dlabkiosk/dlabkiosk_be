CREATE TABLE exam_schedules (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    store_id   BIGINT       NOT NULL,
    exam_name  VARCHAR(100) NOT NULL,
    exam_date  DATE         NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exam_schedule_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE INDEX idx_exam_schedule_store_date ON exam_schedules (store_id, exam_date);
