CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    real_name VARCHAR(64) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_code VARCHAR(64) NOT NULL,
    customer_type VARCHAR(32) NOT NULL,
    nickname VARCHAR(128) NOT NULL,
    contact_value VARCHAR(128) NOT NULL,
    source_platform VARCHAR(64) NOT NULL,
    added_at DATETIME NULL,
    is_nuist_freshman INT NOT NULL DEFAULT 0,
    expected_major VARCHAR(128) NULL,
    base_level VARCHAR(64) NULL,
    interest_direction VARCHAR(255) NULL,
    intent_level VARCHAR(32) NOT NULL,
    current_stage VARCHAR(64) NOT NULL,
    current_concern VARCHAR(255) NULL,
    latest_feedback VARCHAR(500) NULL,
    last_contact_at DATETIME NULL,
    next_follow_action VARCHAR(255) NULL,
    next_follow_at DATETIME NULL,
    owner_user_id BIGINT NOT NULL,
    assistant_user_id BIGINT NULL,
    needs_support INT NOT NULL DEFAULT 0,
    conversion_result VARCHAR(64) NULL,
    remarks TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_customers_customer_code (customer_code)
);

CREATE TABLE IF NOT EXISTS customer_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(64) NOT NULL,
    tag_type VARCHAR(32) NOT NULL,
    tag_color VARCHAR(32) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_customer_tags_name_type (tag_name, tag_type)
);

CREATE TABLE IF NOT EXISTS customer_tag_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_tag_relations (customer_id, tag_id)
);

CREATE TABLE IF NOT EXISTS customer_follow_ups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    follow_type VARCHAR(32) NOT NULL,
    communicated_content TEXT NOT NULL,
    customer_feedback VARCHAR(500) NULL,
    current_concern VARCHAR(255) NULL,
    next_action VARCHAR(255) NULL,
    next_follow_at DATETIME NULL,
    operator_user_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    stage_before VARCHAR(64) NULL,
    stage_after VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0
);
