CREATE TABLE seat_leave_reasons
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id      BIGINT      NOT NULL,
    reason_name   VARCHAR(50) NOT NULL,
    display_order INT         NOT NULL DEFAULT 0,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_leave_reasons_store FOREIGN KEY (store_id) REFERENCES stores (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 시드 데이터 제거 (DSA 동기화로 대체)
