CREATE TABLE students (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id   BIGINT       NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    qr_uuid    VARCHAR(36)  NOT NULL UNIQUE,
    grade      VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_store FOREIGN KEY (store_id) REFERENCES stores (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
