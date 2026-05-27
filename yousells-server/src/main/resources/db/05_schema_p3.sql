SET NAMES utf8mb4;

-- ============================================================
-- YouSells P3 Schema — AI 智能化升级
-- ============================================================

-- 1. AI 客户洞察缓存表（P3-AI-INSIGHT · 志明）
CREATE TABLE IF NOT EXISTS ai_insight_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL COMMENT '关联客户',
    insight_json TEXT NOT NULL COMMENT 'AI 洞察结果 JSON',
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_insight_customer (customer_id),
    KEY idx_insight_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 客户流失风险表（P3-AI-CHURN · 嘉诚）
CREATE TABLE IF NOT EXISTS ai_churn_risk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL COMMENT '关联客户',
    risk_level VARCHAR(8) NOT NULL COMMENT '高风险/中风险/低风险',
    risk_score INT NOT NULL DEFAULT 0 COMMENT '风险评分 0-100',
    risk_factors JSON NULL COMMENT '风险因素列表',
    suggestion TEXT NULL COMMENT '干预建议文案',
    evaluated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评估时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_churn_customer (customer_id),
    KEY idx_churn_level (risk_level),
    KEY idx_churn_evaluated (evaluated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
