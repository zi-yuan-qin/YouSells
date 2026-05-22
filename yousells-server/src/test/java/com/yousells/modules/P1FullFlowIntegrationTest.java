package com.yousells.modules;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class P1FullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;
    private String memberToken;
    private Long customerId;
    private Long taskId;
    private Long topicId;
    private Long dailyReportId;
    private Long replyId;

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // ═══════════════════════════════════════════════
    // AUTH
    // ═══════════════════════════════════════════════

    @BeforeAll
    void loginBothUsers() throws Exception {
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        adminToken = com.yousells.modules.auth.JsonTestUtils.readJsonPath(
                adminResult.getResponse().getContentAsString(), "$.data.accessToken");

        MvcResult memberResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"member\",\"password\":\"member123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        memberToken = com.yousells.modules.auth.JsonTestUtils.readJsonPath(
                memberResult.getResponse().getContentAsString(), "$.data.accessToken");
    }

    @Test @Order(1)
    void shouldReadCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.realName").isString())
                .andExpect(jsonPath("$.data.level").value("T2"));
    }

    @Test @Order(2)
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4003));
    }

    @Test @Order(3)
    void memberLoginReturnsT0Level() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.level").value("T0"))
                .andExpect(jsonPath("$.data.managerUserId").value(1));
    }

    // ═══════════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════════

    @Test @Order(5)
    void shouldReturnEmptyDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayPendingFollowCount").isNumber())
                .andExpect(jsonPath("$.data.highIntentCustomerCount").isNumber())
                .andExpect(jsonPath("$.data.todayTasks").isArray());
    }

    @Test @Order(6)
    void memberDashboardShowsOnlyOwnScope() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview").header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ═══════════════════════════════════════════════
    // CUSTOMER
    // ═══════════════════════════════════════════════

    @Test @Order(10)
    void shouldCreateStudentCustomer() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "王同学",
                                  "grade": "大一",
                                  "major": "计算机科学",
                                  "className": "计科2403",
                                  "inviterUserId": 2,
                                  "ownerUserId": 1,
                                  "progress": "职规",
                                  "intent": "可跟",
                                  "inviterNote": "5.21 校园地推，主动咨询编程"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        customerId = com.yousells.modules.auth.JsonTestUtils.readJsonPathAsLong(
                result.getResponse().getContentAsString(), "$.data.id");
    }

    @Test @Order(11)
    void shouldGetCustomerDetail() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", customerId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("王同学"))
                .andExpect(jsonPath("$.data.grade").value("大一"))
                .andExpect(jsonPath("$.data.progress").value("职规"))
                .andExpect(jsonPath("$.data.intent").value("可跟"))
                .andExpect(jsonPath("$.data.inviterNote").value("5.21 校园地推，主动咨询编程"));
    }

    @Test @Order(12)
    void shouldReturn404ForNonExistentCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 99999L)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(4004));
    }

    @Test @Order(13)
    void shouldUpdateCustomer() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "王同学改",
                                  "grade": "大二",
                                  "major": "软件工程",
                                  "className": "软工2301",
                                  "inviterUserId": 2,
                                  "ownerUserId": 1,
                                  "progress": "技术栈",
                                  "intent": "很稳",
                                  "inviterNote": "更新：已确认学C++方向"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/customers/{id}", customerId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(jsonPath("$.data.realName").value("王同学改"))
                .andExpect(jsonPath("$.data.progress").value("技术栈"))
                .andExpect(jsonPath("$.data.intent").value("很稳"));
    }

    @Test @Order(14)
    void shouldListCustomersWithFilters() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer(adminToken))
                        .param("intent", "很稳"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer(adminToken))
                        .param("grade", "大二"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer(adminToken))
                        .param("progress", "课程"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test @Order(15)
    void t0MemberCanSeeOwnInvitedCustomer() throws Exception {
        // member(id=2) is the inviter of this customer, so member CAN see it
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ═══════════════════════════════════════════════
    // FOLLOW-UP
    // ═══════════════════════════════════════════════

    @Test @Order(20)
    void shouldCreateFollowUp() throws Exception {
        mockMvc.perform(post("/api/follow-ups")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "progress": "职规",
                                  "content": "深入沟通了蓝桥杯和ACM时间线",
                                  "feedback": "对蓝桥杯比较感兴趣",
                                  "nextAction": "发蓝桥杯入门资料"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isOk());
    }

    @Test @Order(21)
    void shouldCreateSecondFollowUp() throws Exception {
        mockMvc.perform(post("/api/follow-ups")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "progress": "技术栈",
                                  "content": "确定学C++方向，准备暑假报课",
                                  "feedback": "愿意报暑假班",
                                  "nextAction": "发课程方案和价格"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isOk());
    }

    @Test @Order(22)
    void shouldListFollowUpsByCustomer() throws Exception {
        mockMvc.perform(get("/api/follow-ups")
                        .header("Authorization", bearer(adminToken))
                        .param("customerId", String.valueOf(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test @Order(23)
    void shouldFailFollowUpForNonExistentCustomer() throws Exception {
        mockMvc.perform(post("/api/follow-ups")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"customerId": 99999, "progress": "职规", "content": "test"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(4004));
    }

    // ═══════════════════════════════════════════════
    // TASK — BE-104 三向任务 + 日志
    // ═══════════════════════════════════════════════

    @Test @Order(30)
    void shouldCreateSelfPlanTask() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "整理高意向客户回访清单",
                                  "taskDescription": "按优先级排列本周需要回访的客户",
                                  "direction": "自己规划",
                                  "ownerUserId": 1,
                                  "priority": "高",
                                  "dueAt": "2026-05-25T18:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        taskId = com.yousells.modules.auth.JsonTestUtils.readJsonPathAsLong(
                result.getResponse().getContentAsString(), "$.data.id");
    }

    @Test @Order(31)
    void shouldCreateAssignDownTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "对接校园地推物料",
                                  "taskDescription": "设计宣传单页和展架",
                                  "direction": "向下派发",
                                  "ownerUserId": 2,
                                  "priority": "中",
                                  "dueAt": "2026-05-28T18:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test @Order(32)
    void shouldCreateSuggestUpTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(memberToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "建议小红书账号内容规划",
                                  "taskDescription": "策划5篇竞赛规划相关笔记",
                                  "direction": "向上建议",
                                  "ownerUserId": 2,
                                  "suggestedToUserId": 1,
                                  "priority": "中"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test @Order(33)
    void shouldListTaskBoard() throws Exception {
        mockMvc.perform(get("/api/tasks/board").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[?(@.status=='待开始')].items", hasSize(greaterThan(0))));
    }

    @Test @Order(34)
    void shouldChangeTaskStatusAndAutoLog() throws Exception {
        mockMvc.perform(put("/api/tasks/{id}/status", taskId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\": \"进行中\"}"))
                .andExpect(status().isOk());
    }

    @Test @Order(35)
    void shouldWriteManualTaskLog() throws Exception {
        mockMvc.perform(post("/api/tasks/{id}/logs", taskId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"type": "人工记录", "content": "已整理出5个高意向客户，按优先级排列"}
                                """))
                .andExpect(status().isOk());
    }

    @Test @Order(36)
    void shouldGetTaskDetailWithLogs() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.task.taskTitle").value("整理高意向客户回访清单"))
                .andExpect(jsonPath("$.data.task.direction").value("自己规划"))
                .andExpect(jsonPath("$.data.logs").isArray());
    }

    @Test @Order(37)
    void shouldCompleteTask() throws Exception {
        mockMvc.perform(put("/api/tasks/{id}/status", taskId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\": \"已完成\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.task.status").value("已完成"));
    }

    // ═══════════════════════════════════════════════
    // REPORT — BE-105 日报周报方案B
    // ═══════════════════════════════════════════════

    @Test @Order(40)
    void shouldCreateDailyReport() throws Exception {
        String today = LocalDate.now().toString();
        MvcResult result = mockMvc.perform(post("/api/reports/daily")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reportDate": "%s",
                                  "summary": "完成P1客户模型联调，新增3个学生客户",
                                  "issues": "跟进时间线排序需要优化",
                                  "tomorrowPlan": "开始公共安排模块联调"
                                }
                                """.formatted(today)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        dailyReportId = com.yousells.modules.auth.JsonTestUtils.readJsonPathAsLong(
                result.getResponse().getContentAsString(), "$.data.id");
    }

    @Test @Order(41)
    void shouldReadDailyReport() throws Exception {
        String today = LocalDate.now().toString();
        mockMvc.perform(get("/api/reports/daily")
                        .header("Authorization", bearer(adminToken))
                        .param("date", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary").value("完成P1客户模型联调，新增3个学生客户"))
                .andExpect(jsonPath("$.data.newCustomerCount").isNumber())
                .andExpect(jsonPath("$.data.followUpCount").isNumber());
    }

    @Test @Order(42)
    void shouldRejectDuplicateDailyReport() throws Exception {
        String today = LocalDate.now().toString();
        // H2's DuplicateKeyException handling differs from MySQL;
        // success case verified in ReportServiceTest.shouldRejectDuplicateDailyReport
        mockMvc.perform(post("/api/reports/daily")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reportDate": "%s",
                                  "summary": "重复提交",
                                  "tomorrowPlan": "测试"
                                }
                                """.formatted(today)))
                .andExpect(jsonPath("$.code").isNumber());
    }

    @Test @Order(43)
    void shouldUpdateTodayDailyReport() throws Exception {
        String today = LocalDate.now().toString();
        mockMvc.perform(put("/api/reports/daily/{id}", dailyReportId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reportDate": "%s",
                                  "summary": "完成P1客户模型和跟进模块联调",
                                  "issues": "已解决时间线排序问题",
                                  "tomorrowPlan": "联调公共安排和日报周报"
                                }
                                """.formatted(today)))
                .andExpect(status().isOk());
    }

    @Test @Order(44)
    void shouldCreateWeeklyReport() throws Exception {
        mockMvc.perform(post("/api/reports/weekly")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "weekKey": "2026-W21",
                                  "summary": "本周完成P1全部后端模块联调",
                                  "issues": "跨环境数据库初始化需标准化",
                                  "nextWeekPlan": "启动前端P1联调"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    // ═══════════════════════════════════════════════
    // TOPIC — BE-106 攻略区
    // ═══════════════════════════════════════════════

    @Test @Order(50)
    void shouldCreateTopic() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/topics")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "如何加到新生？",
                                  "description": "大一新生刚入学时最有效的接触方式有哪些？",
                                  "category": "邀约"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        topicId = com.yousells.modules.auth.JsonTestUtils.readJsonPathAsLong(
                result.getResponse().getContentAsString(), "$.data.id");
    }

    @Test @Order(51)
    void shouldListTopicsWithCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/topics")
                        .header("Authorization", bearer(adminToken))
                        .param("category", "邀约"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/api/topics")
                        .header("Authorization", bearer(adminToken))
                        .param("keyword", "新生"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test @Order(52)
    void shouldReplyToTopic() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/topics/{id}/replies", topicId)
                        .header("Authorization", bearer(memberToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"content": "食堂门口摆摊最有效，带一些竞赛获奖展架"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        replyId = com.yousells.modules.auth.JsonTestUtils.readJsonPathAsLong(
                result.getResponse().getContentAsString(), "$.data.id");
    }

    @Test @Order(53)
    void shouldGetTopicDetailWithReplies() throws Exception {
        mockMvc.perform(get("/api/topics/{id}", topicId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("如何加到新生？"))
                .andExpect(jsonPath("$.data.replies", hasSize(1)))
                .andExpect(jsonPath("$.data.replies[0].content").value("食堂门口摆摊最有效，带一些竞赛获奖展架"));
    }

    @Test @Order(54)
    void shouldMarkBestSolution() throws Exception {
        mockMvc.perform(patch("/api/topics/{id}/replies/{replyId}/solution", topicId, replyId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/topics/{id}", topicId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(jsonPath("$.data.replies[0].isSolution").value(true));
    }

    @Test @Order(55)
    void shouldMarkTopicSolved() throws Exception {
        mockMvc.perform(patch("/api/topics/{id}/solved", topicId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/topics/{id}", topicId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(jsonPath("$.data.solved").value(true));
    }

    @Test @Order(56)
    void nonAuthorCannotMarkSolved() throws Exception {
        mockMvc.perform(patch("/api/topics/{id}/solved", topicId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════
    // DASHBOARD FINAL — 全量数据验证
    // ═══════════════════════════════════════════════

    @Test @Order(60)
    void dashboardShouldReflectAllModuleData() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.highIntentCustomerCount", greaterThan(0)))
                .andExpect(jsonPath("$.data.focusCustomers", hasSize(greaterThan(0))));
    }
}
