-- ADMIN(통합관리자)은 특정 지점에 묶이지 않으므로 store_id NULL 허용
ALTER TABLE admin_users MODIFY COLUMN store_id BIGINT NULL;