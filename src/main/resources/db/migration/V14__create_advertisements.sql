CREATE TABLE advertisements
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id        BIGINT       NOT NULL,
    image_url       VARCHAR(500) NOT NULL,
    media_type      VARCHAR(10)  NOT NULL DEFAULT 'IMAGE',
    display_order   INT          NOT NULL DEFAULT 0,
    display_seconds INT          NOT NULL DEFAULT 5,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE INDEX idx_ad_store_active_order ON advertisements (store_id, is_active, display_order);
