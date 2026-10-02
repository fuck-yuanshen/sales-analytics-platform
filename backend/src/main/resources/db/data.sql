INSERT IGNORE INTO users(user_code, user_name, gender, province, city, district, user_tag, spending_tier, created_at)
VALUES
('U1001', 'Alice', 'F', 'Guangdong', 'Guangzhou', 'Tianhe', 'NEW', 'L1', NOW()),
('U1002', 'Bob', 'M', 'Guangdong', 'Shenzhen', 'Nanshan', 'RETURNING', 'L2', NOW()),
('U1003', 'Charlie', 'M', 'Zhejiang', 'Hangzhou', 'Xihu', 'RETURNING', 'L3', NOW()),
('U1004', 'Diana', 'F', 'Sichuan', 'Chengdu', 'Wuhou', 'NEW', 'L1', NOW());

INSERT IGNORE INTO products(sku, product_name, category, unit_price, created_at)
VALUES
('SKU-101', 'Wireless Earbuds', 'Digital', 299.00, NOW()),
('SKU-102', 'Coffee Maker', 'Home', 499.00, NOW()),
('SKU-103', 'Facial Cleanser', 'Beauty', 89.00, NOW()),
('SKU-104', 'Snack Combo Box', 'Food', 59.00, NOW());

INSERT IGNORE INTO orders(order_no, user_id, order_type, payment_status, placed_at, paid_at, total_amount, total_quantity, created_at, updated_at)
VALUES
('ORD-0001', 1, 'NORMAL', 'PAID', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), 598.00, 2, NOW(), NOW()),
('ORD-0002', 2, 'GROUP', 'PAID', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), 499.00, 1, NOW(), NOW()),
('ORD-0003', 2, 'NORMAL', 'PAID', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), 148.00, 2, NOW(), NOW()),
('ORD-0004', 3, 'NORMAL', 'PLACED', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 299.00, 1, NOW(), NOW()),
('ORD-0005', 4, 'NORMAL', 'PAID', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 59.00, 1, NOW(), NOW());

INSERT IGNORE INTO order_items(order_id, product_id, quantity, unit_price, amount)
VALUES
(1, 1, 2, 299.00, 598.00),
(2, 2, 1, 499.00, 499.00),
(3, 3, 1, 89.00, 89.00),
(3, 4, 1, 59.00, 59.00),
(4, 1, 1, 299.00, 299.00),
(5, 4, 1, 59.00, 59.00);

INSERT IGNORE INTO alert_rules(rule_name, metric_code, comparator, threshold, enabled, description, created_at, updated_at)
VALUES
('Sales Drop Alert', 'SALES_MOM_CHANGE', 'LESS_THAN', -20.00, 1, 'Sales month-over-month drop exceeds 20%', NOW(), NOW()),
('Repurchase Warning', 'REPURCHASE_RATE', 'LESS_THAN', 10.00, 1, 'Repurchase rate below 10%', NOW(), NOW());

INSERT INTO system_config(config_key, config_value, updated_at)
VALUES
('retention_days', '180', NOW()),
('sync_frequency', 'DAILY', NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_at = VALUES(updated_at);

