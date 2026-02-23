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

INSERT INTO seats (store_id, seat_label, seat_type, x_pos, y_pos)
VALUES (1, 'A-1', 'INDIVIDUAL', 100, 100),
       (1, 'A-2', 'INDIVIDUAL', 200, 100),
       (1, 'A-3', 'INDIVIDUAL', 300, 100),
       (1, 'B-1', 'INDIVIDUAL', 100, 250),
       (1, 'B-2', 'INDIVIDUAL', 200, 250),
       (2, 'A-1', 'INDIVIDUAL', 100, 100),
       (2, 'A-2', 'INDIVIDUAL', 200, 100),
       (2, 'A-3', 'INDIVIDUAL', 300, 100);
