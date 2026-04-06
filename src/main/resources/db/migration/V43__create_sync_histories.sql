CREATE TABLE sync_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sync_type VARCHAR(20) NOT NULL,
    store_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    total_count INT NOT NULL DEFAULT 0,
    created_count INT NOT NULL DEFAULT 0,
    updated_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000) NULL,
    started_at DATETIME(6) NOT NULL,
    finished_at DATETIME(6) NULL,
    INDEX idx_sync_histories_type_started (sync_type, started_at DESC)
);
