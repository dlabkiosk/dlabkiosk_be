-- 기존 지점 DSA 인증정보 UPDATE (DS-001 ~ DS-005)
UPDATE stores SET dsa_acad_cd = '46', dsa_client_id = 'client9_1', dsa_secret_id = '3A8E1BD005F1D31DAB6B' WHERE store_code = 'DS-001';
UPDATE stores SET dsa_acad_cd = '45', dsa_client_id = 'client8_1', dsa_secret_id = '5FF7FFEF879B28CFD5AF' WHERE store_code = 'DS-002';
UPDATE stores SET dsa_acad_cd = '44', dsa_client_id = 'client7_1', dsa_secret_id = 'BB5ED4380A4DE3E4BEA5' WHERE store_code = 'DS-003';
UPDATE stores SET dsa_acad_cd = '43', dsa_client_id = 'client6_1', dsa_secret_id = '76DDFD73DF78C709A871' WHERE store_code = 'DS-004';
UPDATE stores SET dsa_acad_cd = '42', dsa_client_id = 'client5_1', dsa_secret_id = '0E9F927155F74FFCB02A' WHERE store_code = 'DS-005';

-- 신규 지점 INSERT (없으면 추가)
INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLAB 김포점', 'DS-006', TRUE, '0000', '34', 'client4_1', 'FE7CC3280D2A5544786E'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-006');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLAB 동탄점 (본관, 별관)', 'DS-007', TRUE, '0000', '33', 'client3_1', 'B9B52423ED3572EFA88B'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-007');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLAB 일산점', 'DS-008', TRUE, '0000', '32', 'client2_1', 'd34d21m7d0p6k8j96i5s'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-008');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLAB 분당점', 'DS-009', TRUE, '0000', '31', 'client1_2', '8glzltj96iy8pdu7jahn'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-009');
