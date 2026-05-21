SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    real_name VARCHAR(64) NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(128) NULL,
    status VARCHAR(32) NOT NULL,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_users_username (username),
    KEY idx_users_status (status)
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_roles_role_code (role_code)
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    permission_group VARCHAR(64) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_permissions_code (permission_code)
);

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_roles (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permissions (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_code VARCHAR(64) NOT NULL,
    customer_type VARCHAR(32) NOT NULL,
    nickname VARCHAR(128) NOT NULL,
    contact_value VARCHAR(128) NOT NULL,
    source_platform VARCHAR(64) NOT NULL,
    added_at DATETIME NULL,
    is_nuist_freshman TINYINT(1) NOT NULL DEFAULT 0,
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
    needs_support TINYINT(1) NOT NULL DEFAULT 0,
    conversion_result VARCHAR(64) NULL,
    remarks TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_customers_customer_code (customer_code),
    KEY idx_customers_owner_user_id (owner_user_id),
    KEY idx_customers_current_stage (current_stage),
    KEY idx_customers_intent_level (intent_level),
    KEY idx_customers_next_follow_at (next_follow_at),
    KEY idx_customers_source_platform (source_platform)
);

CREATE TABLE IF NOT EXISTS customer_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(64) NOT NULL,
    tag_type VARCHAR(32) NOT NULL,
    tag_color VARCHAR(32) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_customer_tags_name_type (tag_name, tag_type)
);

CREATE TABLE IF NOT EXISTS customer_tag_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_tag_relations (customer_id, tag_id),
    KEY idx_customer_tag_relations_tag_id (tag_id)
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
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_customer_follow_ups_customer_id (customer_id),
    KEY idx_customer_follow_ups_created_at (created_at),
    KEY idx_customer_follow_ups_operator_user_id (operator_user_id)
);

CREATE TABLE IF NOT EXISTS task_boards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_title VARCHAR(255) NOT NULL,
    task_type VARCHAR(64) NULL,
    task_description TEXT NULL,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    assistant_user_id BIGINT NULL,
    start_at DATETIME NULL,
    due_at DATETIME NULL,
    next_action VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_task_boards_owner_user_id (owner_user_id),
    KEY idx_task_boards_status (status),
    KEY idx_task_boards_due_at (due_at)
);

CREATE TABLE IF NOT EXISTS daily_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_date DATE NOT NULL,
    user_id BIGINT NOT NULL,
    today_work TEXT NOT NULL,
    issues TEXT NULL,
    tomorrow_plan TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_daily_reports (report_date, user_id),
    KEY idx_daily_reports_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS weekly_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_key VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    weekly_summary TEXT NOT NULL,
    issues TEXT NULL,
    next_week_plan TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_weekly_reports (week_key, user_id),
    KEY idx_weekly_reports_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS script_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(64) NOT NULL,
    category_name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_script_categories_code (category_code)
);

CREATE TABLE IF NOT EXISTS scripts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    applicable_scene VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_scripts_category_id (category_id),
    KEY idx_scripts_status (status)
);

CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_user_id BIGINT NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    business_id BIGINT NOT NULL,
    operation_type VARCHAR(64) NOT NULL,
    operation_summary VARCHAR(255) NOT NULL,
    operation_detail TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_operation_logs_operator_user_id (operator_user_id),
    KEY idx_operation_logs_business_type_business_id (business_type, business_id)
);
