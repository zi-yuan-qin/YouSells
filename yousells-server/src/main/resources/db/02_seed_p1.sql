SET NAMES utf8mb4;

-- ============================================================
-- YouSells P1 种子数据
-- ============================================================

-- 1. 用户种子（2人：T2 队长 + T0 成员）
INSERT INTO users (id, username, password_hash, real_name, level, manager_user_id, status)
VALUES
    (1, 'admin', '$2a$10$tZJK87gM16/lFCEW8f36uen8t2dbOHAFA0a6jKdxUAOBMO2pI72p6', '秦梓源', 'T2', NULL, 'ACTIVE'),
    (2, 'member', '$2a$10$FVamKOa8CyTGdDWBeGd04emjcWCIWEwJgFU.lsGDlHA8qE5SSZ4oa', '小赵', 'T0', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), level = VALUES(level), manager_user_id = VALUES(manager_user_id);

-- 2. 客户种子（5个学生）
INSERT INTO customers (id, real_name, grade, major, class_name, inviter_user_id, owner_user_id, progress, intent, inviter_note, created_by, updated_by)
VALUES
    (1, '王同学', '大一', '计算机科学', '计科2403', 2, 1, '技术栈', '可跟',
     '5.21 校园地推，在食堂门口主动过来咨询。高中打过NOIP，大学想继续搞竞赛但不确定方向。人比较腼腆，但聊到技术的时候眼睛会发光。建议从职规切入先帮他梳理方向。',
     1, 1),
    (2, '张同学', '大二', '软件工程', '软工2301', 1, 1, '课程', '很稳',
     '知乎私信过来的，目标明确想要项目加分，对价格不敏感，需要看到成功案例。已发保研案例给他。',
     1, 1),
    (3, '李同学', '大一', '数学应用', '数学2401', 2, 1, '职规', '观望',
     '抖音过来的大一新生，问了竞赛加分政策，给了政策文档说回去看看，还没确定方向。',
     1, 1),
    (4, '刘同学', '大三', '通信工程', '通信2202', 1, 2, '技术栈', '可跟',
     '小红书来的，大三才开始准备觉得太晚。已经发了逆袭案例激励他，需要持续跟进时间管理方案。',
     1, 1),
    (5, '陈同学', '大一', '人工智能', 'AI2401', 2, 2, '职规', '观望',
     '地推线下加的好友，加了社团担心时间冲突。信任度高，需要拉他进新生规划群。',
     1, 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), progress = VALUES(progress), intent = VALUES(intent);

-- 3. 跟进记录种子（3条）
INSERT INTO customer_follow_ups (id, customer_id, user_id, progress, content, feedback, next_action)
VALUES
    (1, 1, 1, '职规', '通过小赵的邀约备注了解了背景，晚上微信约王同学聊了20分钟。聊了大一可以做哪些竞赛方向，蓝桥杯、ACM的时间线，他对蓝桥杯比较感兴趣。',
     '说想试试蓝桥杯，下学期开始准备', '下周发一份蓝桥杯入门资料，约第二次聊技术栈'),
    (2, 1, 1, '技术栈', '发了蓝桥杯入门资料，讲了C++/Java/Python三条路线的区别，他确定学C++，暑假可以报课。',
     '确定学C++，暑假报课', '发课程方案和价格'),
    (3, 2, 1, '课程', '发了两个班型对比，张同学选了冲刺班，直接付款了。确认已付款到账，对接课程安排。',
     '很满意', '安排进入课程群')
ON DUPLICATE KEY UPDATE content = VALUES(content);

-- 4. 任务种子（5条）
INSERT INTO task_boards (id, task_title, task_description, direction, owner_user_id, creator_user_id, suggested_to_user_id, status, priority, due_at, created_by, updated_by)
VALUES
    (1, '整理攻略区分类与模板', '按邀约、沟通、跟进、转化四个场景整理常用攻略模板', '自己规划', 1, 1, NULL, '进行中', '高', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, 1),
    (2, '对接校园地推物料', '设计宣传单页和展架内容，联系打印店报价', '向下派发', 2, 1, NULL, '待开始', '中', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 1, 1),
    (3, '知乎引流测试', '在知乎回答3个竞赛相关热门问题，测试引流效果', '自己规划', 2, 2, NULL, '待开始', '低', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, 1),
    (4, '小红书账号内容规划', '策划5篇竞赛规划相关笔记，安排发布时间表', '向上建议', 1, 2, 1, '待开始', '中', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 1, 1),
    (5, '客户跟进SOP编写', '写出标准客户跟进流程文档，覆盖首轮到课程的全链路', '自己规划', 1, 1, NULL, '已完成', '高', DATE_ADD(CURDATE(), INTERVAL -1 DAY), 1, 1)
ON DUPLICATE KEY UPDATE task_title = VALUES(task_title);
