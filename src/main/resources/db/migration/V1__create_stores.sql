CREATE TABLE stores (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_name VARCHAR(100) NOT NULL,
    store_code VARCHAR(20)  NOT NULL UNIQUE,
    address    VARCHAR(255),
    phone      VARCHAR(20),
    is_active  BOOLEAN    NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO stores (store_name, store_code, address, phone) VALUES
    ('대성 본점', 'DS-001', '서울특별시 강남구 테헤란로 123', '02-1234-5678'),
    ('대성 강북점', 'DS-002', '서울특별시 종로구 종로 45', '02-2345-6789'),
    ('대성 인천점', 'DS-003', '인천광역시 남동구 구월로 67', '032-345-6789'),
    ('대성 수원점', 'DS-004', '경기도 수원시 영통구 광교로 89', '031-456-7890'),
    ('대성 부산점', 'DS-005', '부산광역시 해운대구 해운대로 101', '051-567-8901');
