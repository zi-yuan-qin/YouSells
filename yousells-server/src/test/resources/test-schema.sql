-- P1 Test Schema (H2 in-memory)

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    level VARCHAR(4) NOT NULL DEFAULT 'T0',
    manager_user_id BIGINT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    resign_reason VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    real_name VARCHAR(64) NOT NULL,
    grade VARCHAR(16) NOT NULL,
    major VARCHAR(128) NOT NULL,
    class_name VARCHAR(64) NULL,
    phone VARCHAR(20) NULL,
    wechat VARCHAR(64) NULL,
    inviter_user_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    progress VARCHAR(16) NOT NULL DEFAULT '职规',
    intent VARCHAR(8) NOT NULL DEFAULT '观望',
    inviter_note TEXT NULL,
    source_channel VARCHAR(32) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS customer_follow_ups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    progress VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    feedback VARCHAR(500) NULL,
    next_action VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS task_boards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_title VARCHAR(255) NOT NULL,
    task_description TEXT NULL,
    direction VARCHAR(8) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    creator_user_id BIGINT NOT NULL,
    suggested_to_user_id BIGINT NULL,
    status VARCHAR(8) NOT NULL DEFAULT '待开始',
    priority VARCHAR(4) NOT NULL DEFAULT '中',
    due_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS task_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(8) NOT NULL,
    content TEXT NULL,
    from_status VARCHAR(8) NULL,
    to_status VARCHAR(8) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS daily_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_date DATE NOT NULL,
    user_id BIGINT NOT NULL,
    new_customer_count INT NOT NULL DEFAULT 0,
    follow_up_count INT NOT NULL DEFAULT 0,
    progress_advance_count INT NOT NULL DEFAULT 0,
    task_completed_count INT NOT NULL DEFAULT 0,
    progress_details TEXT NULL,
    task_completed_details TEXT NULL,
    summary TEXT NOT NULL,
    issues TEXT NULL,
    tomorrow_plan TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS weekly_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_key VARCHAR(8) NOT NULL,
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    user_id BIGINT NOT NULL,
    new_customer_count INT NOT NULL DEFAULT 0,
    follow_up_count INT NOT NULL DEFAULT 0,
    progress_advance_count INT NOT NULL DEFAULT 0,
    converted_count INT NOT NULL DEFAULT 0,
    task_completed_count INT NOT NULL DEFAULT 0,
    progress_details TEXT NULL,
    converted_details TEXT NULL,
    task_completed_details TEXT NULL,
    summary TEXT NOT NULL,
    issues TEXT NULL,
    next_week_plan TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    category VARCHAR(16) NOT NULL,
    author_user_id BIGINT NOT NULL,
    solved INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS topic_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_solution INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_user_id BIGINT NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    business_id BIGINT NOT NULL,
    operation_type VARCHAR(64) NOT NULL,
    operation_summary VARCHAR(255) NOT NULL,
    operation_detail TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NULL,
    business_type VARCHAR(32) NULL,
    business_id BIGINT NULL,
    is_read INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS report_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    report_type VARCHAR(8) NOT NULL,
    report_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, report_type, report_id)
);

CREATE TABLE IF NOT EXISTS report_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    report_type VARCHAR(8) NOT NULL,
    report_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ai_insight_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    insight_json TEXT NOT NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_insight_customer (customer_id)
);

DELETE FROM ai_insight_cache;
DELETE FROM notifications;
DELETE FROM operation_logs;
DELETE FROM weekly_reports;
DELETE FROM daily_reports;
DELETE FROM task_logs;
DELETE FROM task_boards;
DELETE FROM customer_follow_ups;
DELETE FROM customers;
DELETE FROM users;

INSERT INTO users (id, username, password_hash, real_name, level, manager_user_id, status)
VALUES
    (1, 'admin', '$2a$10$tZJK87gM16/lFCEW8f36uen8t2dbOHAFA0a6jKdxUAOBMO2pI72p6', '秦梓源', 'T2', NULL, 'ACTIVE'),
    (2, 'member', '$2a$10$FVamKOa8CyTGdDWBeGd04emjcWCIWEwJgFU.lsGDlHA8qE5SSZ4oa', '小赵', 'T0', 1, 'ACTIVE');
