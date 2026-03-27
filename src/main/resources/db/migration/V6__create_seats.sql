CREATE TABLE seats
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id   BIGINT      NOT NULL,
    seat_label VARCHAR(20) NOT NULL,
    seat_type  VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',
    x_pos      INT         NOT NULL DEFAULT 0,
    y_pos      INT         NOT NULL DEFAULT 0,
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seats_store FOREIGN KEY (store_id) REFERENCES stores (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 시드 데이터 제거 (DSA 동기화로 대체)
