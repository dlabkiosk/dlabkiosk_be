CREATE TABLE notices
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id   BIGINT       NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    is_pinned  TINYINT(1)   NOT NULL DEFAULT 0,
    is_active  TINYINT(1)   NOT NULL DEFAULT 1,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE INDEX idx_notice_store_active_pinned ON notices (store_id, is_active, is_pinned DESC, created_at DESC);
