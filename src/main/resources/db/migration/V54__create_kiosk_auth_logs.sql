CREATE TABLE kiosk_auth_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NULL,
    store_code VARCHAR(50) NULL,
    event_type VARCHAR(30) NOT NULL,
    reason VARCHAR(50) NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_kiosk_auth_logs_created_at (created_at),
    INDEX idx_kiosk_auth_logs_store_event (store_id, event_type, created_at),
    INDEX idx_kiosk_auth_logs_event (event_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
