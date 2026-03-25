-- 개발 환경 지점 시드 데이터 (지점당 첫 번째 credential)
INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 분당', 'DS-001', TRUE, '0000', '31', 'client1_1', 'T2GT39EF8J9CB86D5Y84'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-001');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 일산', 'DS-002', TRUE, '0000', '32', 'client2_1', 'D34D21M7D0P6K8J96I5S'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-002');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 동탄', 'DS-003', TRUE, '0000', '33', 'client3_1', 'B9B52423ED3572EFA88B'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-003');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 김포', 'DS-004', TRUE, '0000', '34', 'client4_1', 'FE7CC3280D2A5544786E'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-004');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 부천', 'DS-005', TRUE, '0000', '42', 'client5_1', '0E9F927155F74FFCB02A'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-005');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 이매', 'DS-006', TRUE, '0000', '43', 'client6_1', '76DDFD73DF78C709A871'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-006');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 광명', 'DS-007', TRUE, '0000', '44', 'client7_1', 'BB5ED4380A4DE3E4BEA5'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-007');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 목동', 'DS-008', TRUE, '0000', '45', 'client8_1', '5FF7FFEF879B28CFD5AF'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-008');

INSERT INTO stores (store_name, store_code, is_active, kiosk_pin, dsa_acad_cd, dsa_client_id, dsa_secret_id)
SELECT 'DLab 송파', 'DS-009', TRUE, '0000', '46', 'client9_1', '3A8E1BD005F1D31DAB6B'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stores WHERE store_code = 'DS-009');
