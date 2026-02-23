CREATE TABLE outings
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT       NOT NULL,
    store_id   BIGINT       NOT NULL,
    reason     VARCHAR(100),
    started_at DATETIME(6)  NOT NULL,
    ended_at   DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (student_id) REFERENCES students (id),
    FOREIGN KEY (store_id) REFERENCES stores (id)
);
