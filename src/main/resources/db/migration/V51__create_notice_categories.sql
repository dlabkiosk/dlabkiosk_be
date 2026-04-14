CREATE TABLE notice_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    name VARCHAR(30) NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notice_categories_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT uk_notice_categories_store_name UNIQUE (store_id, name),
    INDEX idx_notice_categories_store_order (store_id, display_order)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

ALTER TABLE notices
    ADD COLUMN category_id BIGINT NULL,
    ADD CONSTRAINT fk_notices_category FOREIGN KEY (category_id) REFERENCES notice_categories (id);
