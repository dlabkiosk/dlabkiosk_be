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

INSERT INTO students (store_id, name, phone, qr_uuid, grade) VALUES
    (1, '김민준', '010-1234-5678', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', '고1'),
    (1, '이서윤', '010-2345-6789', 'a1b2c3d4-e5f6-7890-abcd-ef1234567802', '고2'),
    (1, '박지호', '010-3456-7890', 'a1b2c3d4-e5f6-7890-abcd-ef1234567803', '고3'),
    (2, '최수아', '010-4567-8901', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', '중3'),
    (2, '정하준', '010-5678-9012', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', '고1'),
    (3, '강예은', '010-6789-0123', 'a1b2c3d4-e5f6-7890-abcd-ef1234567806', '고2'),
    (3, '조도윤', '010-7890-1234', 'a1b2c3d4-e5f6-7890-abcd-ef1234567807', '재수'),
    (4, '윤서준', '010-8901-2345', 'a1b2c3d4-e5f6-7890-abcd-ef1234567808', '고1'),
    (4, '임하은', '010-9012-3456', 'a1b2c3d4-e5f6-7890-abcd-ef1234567809', '고3'),
    (5, '한지민', '010-0123-4567', 'a1b2c3d4-e5f6-7890-abcd-ef1234567810', '중3');
