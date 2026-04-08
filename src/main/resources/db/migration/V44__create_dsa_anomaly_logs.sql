CREATE TABLE dsa_anomaly_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NULL,
    rfid_uid VARCHAR(100) NULL,
    api_path VARCHAR(200) NOT NULL,
    anomaly_type VARCHAR(50) NOT NULL,
    request_params TEXT NULL,
    raw_response TEXT NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_dsa_anomaly_logs_created (created_at DESC),
    INDEX idx_dsa_anomaly_logs_store_created (store_id, created_at DESC)
);
