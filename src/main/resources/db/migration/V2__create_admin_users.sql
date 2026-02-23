CREATE TABLE admin_users (
                             id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                             login_id   VARCHAR(50)  NOT NULL UNIQUE,
                             password   VARCHAR(255) NOT NULL,
                             name       VARCHAR(50)  NOT NULL,
                             role       VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
                             created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
