ALTER TABLE stores
    ADD COLUMN dsa_acad_cd VARCHAR(50) NULL COMMENT 'DSA 학원 고유코드',
    ADD COLUMN dsa_client_id VARCHAR(100) NULL COMMENT 'DSA 키오스크 ID',
    ADD COLUMN dsa_secret_id VARCHAR(200) NULL COMMENT 'DSA 시크릿 키';
