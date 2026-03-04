-- admin_users에 store_id FK 추가 (N:1 관계)
ALTER TABLE admin_users ADD COLUMN store_id BIGINT NULL;

-- 기존 데이터가 있을 경우 첫 번째 지점으로 설정
UPDATE admin_users SET store_id = (SELECT id FROM stores ORDER BY id LIMIT 1) WHERE store_id IS NULL;

-- NOT NULL 제약조건 추가
ALTER TABLE admin_users MODIFY COLUMN store_id BIGINT NOT NULL;

-- FK 제약조건 추가
ALTER TABLE admin_users ADD CONSTRAINT fk_admin_users_store
    FOREIGN KEY (store_id) REFERENCES stores (id);