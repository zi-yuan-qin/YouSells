SET NAMES utf8mb4;

INSERT INTO roles (role_code, role_name, description)
VALUES
    ('ADMIN', '管理员', '拥有项目内全部管理权限'),
    ('MEMBER', '成员', '负责日常跟进、记录和协作')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description);

INSERT INTO permissions (permission_code, permission_name, permission_group, description)
VALUES
    ('customer:read', '查看客户', 'customer', '查看客户列表与详情'),
    ('customer:write', '编辑客户', 'customer', '新增与修改客户'),
    ('followup:write', '新增跟进记录', 'followup', '记录客户沟通历史'),
    ('task:manage', '管理公共安排', 'task', '维护公共安排与状态流转'),
    ('report:submit', '提交日报周报', 'report', '提交日报与周报'),
    ('script:manage', '管理话术库', 'script', '维护话术分类与内容')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name), description = VALUES(description);

INSERT INTO script_categories (category_code, category_name, sort_order)
VALUES
    ('FIRST_ADD', '初次加好友', 1),
    ('GROUP_INVITE', '邀请进群', 2),
    ('HIGH_INTENT', '高意向推进', 3)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), sort_order = VALUES(sort_order);

INSERT INTO customer_tags (tag_name, tag_type, tag_color, sort_order)
VALUES
    ('竞赛意向', 'DIRECTION', '#2563eb', 1),
    ('零基础', 'BASE', '#16a34a', 2),
    ('高意向', 'ACTIVITY', '#dc2626', 3)
ON DUPLICATE KEY UPDATE tag_color = VALUES(tag_color), sort_order = VALUES(sort_order);
