CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_code VARCHAR(64) NOT NULL UNIQUE,
    user_name VARCHAR(128) NOT NULL,
    gender VARCHAR(16),
    province VARCHAR(64),
    city VARCHAR(64),
    district VARCHAR(64),
    user_tag VARCHAR(32),
    spending_tier VARCHAR(32),
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(64) NOT NULL UNIQUE,
    product_name VARCHAR(128) NOT NULL,
    category VARCHAR(64),
    unit_price DECIMAL(18,2),
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    order_type VARCHAR(32),
    payment_status VARCHAR(32),
    placed_at DATETIME NOT NULL,
    paid_at DATETIME,
    total_amount DECIMAL(18,2) NOT NULL,
    total_quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_orders_placed_at (placed_at),
    INDEX idx_orders_payment_status (payment_status),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS archived_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    order_type VARCHAR(32),
    payment_status VARCHAR(32),
    placed_at DATETIME,
    paid_at DATETIME,
    total_amount DECIMAL(18,2),
    total_quantity INT,
    archived_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS archived_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT,
    unit_price DECIMAL(18,2),
    amount DECIMAL(18,2),
    archived_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(128) NOT NULL,
    metric_code VARCHAR(64) NOT NULL,
    comparator VARCHAR(32) NOT NULL,
    threshold DECIMAL(18,4) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(512),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS alert_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL,
    metric_code VARCHAR(64) NOT NULL,
    metric_value DECIMAL(18,4) NOT NULL,
    period_label VARCHAR(64),
    severity VARCHAR(16),
    message VARCHAR(1024),
    triggered_at DATETIME NOT NULL,
    INDEX idx_alert_events_triggered_at (triggered_at)
);

CREATE TABLE IF NOT EXISTS chart_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_name VARCHAR(128) NOT NULL,
    chart_type VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    color_scheme TEXT,
    axis_config TEXT,
    data_format TEXT,
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS backup_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(256) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    backup_at DATETIME NOT NULL,
    comment VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(64) PRIMARY KEY,
    config_value VARCHAR(512) NOT NULL,
    updated_at DATETIME NOT NULL
);
