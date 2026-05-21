SET NAMES utf8mb4;

-- ============================================================
-- YouSells P1 Schema — 10 张表
-- ============================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    level VARCHAR(4) NOT NULL DEFAULT 'T0' COMMENT '职级：T0/T1/T2/T3',
    manager_user_id BIGINT NULL COMMENT '直属上级',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_users_username (username),
    KEY idx_users_level (level),
    KEY idx_users_manager (manager_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 客户表（学生模型）
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    grade VARCHAR(16) NOT NULL COMMENT '大一/大二/大三/大四',
    major VARCHAR(128) NOT NULL COMMENT '专业',
    class_name VARCHAR(64) NULL COMMENT '班级',
    inviter_user_id BIGINT NOT NULL COMMENT '邀约人',
    owner_user_id BIGINT NOT NULL COMMENT '负责人',
    progress VARCHAR(16) NOT NULL DEFAULT '职规' COMMENT '职规/技术栈/课程',
    intent VARCHAR(8) NOT NULL DEFAULT '观望' COMMENT '很稳/可跟/观望/冷淡',
    inviter_note TEXT NULL COMMENT '邀约备注（一次性填写）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_customers_owner (owner_user_id),
    KEY idx_customers_inviter (inviter_user_id),
    KEY idx_customers_progress (progress),
    KEY idx_customers_intent (intent),
    KEY idx_customers_grade (grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 客户跟进记录表
CREATE TABLE IF NOT EXISTS customer_follow_ups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL COMMENT '关联客户',
    user_id BIGINT NOT NULL COMMENT '写记录的人（邀约人或负责人）',
    progress VARCHAR(16) NOT NULL COMMENT '本次沟通时的进度',
    content TEXT NOT NULL COMMENT '沟通内容',
    feedback VARCHAR(500) NULL COMMENT '学生反馈',
    next_action VARCHAR(255) NULL COMMENT '下一步动作',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_follow_ups_customer (customer_id),
    KEY idx_follow_ups_user (user_id),
    KEY idx_follow_ups_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 任务表（三向）
CREATE TABLE IF NOT EXISTS task_boards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_title VARCHAR(255) NOT NULL COMMENT '标题',
    task_description TEXT NULL COMMENT '详细说明',
    direction VARCHAR(8) NOT NULL COMMENT '向下派发/自己规划/向上建议',
    owner_user_id BIGINT NOT NULL COMMENT '执行人',
    creator_user_id BIGINT NOT NULL COMMENT '创建人',
    suggested_to_user_id BIGINT NULL COMMENT '建议给谁（仅向上建议）',
    status VARCHAR(8) NOT NULL DEFAULT '待开始' COMMENT '待开始/进行中/已完成',
    priority VARCHAR(4) NOT NULL DEFAULT '中' COMMENT '高/中/低',
    due_at DATETIME NULL COMMENT '截止时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_tasks_owner (owner_user_id),
    KEY idx_tasks_creator (creator_user_id),
    KEY idx_tasks_status (status),
    KEY idx_tasks_due (due_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 任务进度日志表
CREATE TABLE IF NOT EXISTS task_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '关联任务',
    user_id BIGINT NOT NULL COMMENT '谁触发的',
    type VARCHAR(8) NOT NULL COMMENT '人工记录/状态变更',
    content TEXT NULL COMMENT '人工写的话',
    from_status VARCHAR(8) NULL COMMENT '变更前状态（仅状态变更）',
    to_status VARCHAR(8) NULL COMMENT '变更后状态（仅状态变更）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_task_logs_task (task_id),
    KEY idx_task_logs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 日报表（方案B）
CREATE TABLE IF NOT EXISTS daily_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_date DATE NOT NULL,
    user_id BIGINT NOT NULL,
    new_customer_count INT NOT NULL DEFAULT 0 COMMENT '今日新增客户数',
    follow_up_count INT NOT NULL DEFAULT 0 COMMENT '今日跟进沟通次数',
    progress_advance_count INT NOT NULL DEFAULT 0 COMMENT '今日进度推进数',
    task_completed_count INT NOT NULL DEFAULT 0 COMMENT '今日完成任务数',
    progress_details TEXT NULL COMMENT '进度推进明细(JSON)',
    task_completed_details TEXT NULL COMMENT '完成任务明细(JSON)',
    summary TEXT NOT NULL COMMENT '今日工作小结',
    issues TEXT NULL COMMENT '遇到的问题',
    tomorrow_plan TEXT NOT NULL COMMENT '明天计划',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_daily_reports (report_date, user_id),
    KEY idx_daily_reports_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 周报表（方案B）
CREATE TABLE IF NOT EXISTS weekly_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_key VARCHAR(8) NOT NULL COMMENT '2026-W21',
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    user_id BIGINT NOT NULL,
    new_customer_count INT NOT NULL DEFAULT 0 COMMENT '本周新增客户数',
    follow_up_count INT NOT NULL DEFAULT 0 COMMENT '本周跟进总次数',
    progress_advance_count INT NOT NULL DEFAULT 0 COMMENT '本周进度推进数',
    converted_count INT NOT NULL DEFAULT 0 COMMENT '本周课程达成数',
    task_completed_count INT NOT NULL DEFAULT 0 COMMENT '本周完成任务数',
    progress_details TEXT NULL COMMENT '进度推进明细(JSON)',
    converted_details TEXT NULL COMMENT '课程达成明细(JSON)',
    task_completed_details TEXT NULL COMMENT '完成任务明细(JSON)',
    summary TEXT NOT NULL COMMENT '本周总结',
    issues TEXT NULL COMMENT '问题复盘',
    next_week_plan TEXT NOT NULL COMMENT '下周计划',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_weekly_reports (week_key, user_id),
    KEY idx_weekly_reports_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 攻略区-问题表
CREATE TABLE IF NOT EXISTS topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '标题',
    description TEXT NULL COMMENT '详细描述',
    category VARCHAR(16) NOT NULL COMMENT '邀约/沟通/跟进/转化/其他',
    author_user_id BIGINT NOT NULL COMMENT '提问人',
    solved TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已解决',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_topics_category (category),
    KEY idx_topics_author (author_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 攻略区-回答表
CREATE TABLE IF NOT EXISTS topic_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT NOT NULL COMMENT '关联问题',
    user_id BIGINT NOT NULL COMMENT '回答人',
    content TEXT NOT NULL COMMENT '方案内容',
    is_solution TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否最佳方案',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_topic_replies_topic (topic_id),
    KEY idx_topic_replies_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_user_id BIGINT NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    business_id BIGINT NOT NULL,
    operation_type VARCHAR(64) NOT NULL,
    operation_summary VARCHAR(255) NOT NULL,
    operation_detail TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_op_logs_operator (operator_user_id),
    KEY idx_op_logs_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
