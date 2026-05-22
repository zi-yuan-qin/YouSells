package com.yousells.modules;

import com.yousells.modules.auth.JsonTestUtils;
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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class P0FullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String accessToken;
    private Long customerId;
    @SuppressWarnings("unused")
    private Long taskId;

    private String bearer() {
        return "Bearer " + accessToken;
    }

    @BeforeAll
    void login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        accessToken = JsonTestUtils.readJsonPath(
                result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    // ═══════════ AUTH ═══════════

    @Test @Order(1)
    void shouldReadCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test @Order(2)
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4003));
    }

    // ═══════════ DASHBOARD ═══════════

    @Test @Order(3)
    void shouldReturnEmptyDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayTasks").isArray());
    }

    // ═══════════ CUSTOMER ═══════════

    @Test @Order(10)
    void shouldCreateCustomer() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearer())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "测试客户A",
                                  "grade": "大一",
                                  "major": "计算机科学",
                                  "className": "计科2403",
                                  "inviterUserId": 1,
                                  "ownerUserId": 1,
                                  "progress": "职规",
                                  "intent": "可跟",
                                  "inviterNote": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        customerId = JsonTestUtils.readJsonPathAsLong(
                result.getResponse().getContentAsString(), "$.data.id");
    }

    @Test @Order(11)
    void shouldGetCustomerDetail() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", customerId).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("测试客户A"))
                .andExpect(jsonPath("$.data.grade").value("大一"));
    }

    @Test @Order(12)
    void shouldReturn404ForNonExistentCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 99999L).header("Authorization", bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(4004));
    }

    @Test @Order(13)
    void shouldUpdateCustomer() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .header("Authorization", bearer())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "测试客户A改",
                                  "grade": "大二",
                                  "major": "软件工程",
                                  "className": "软工2301",
                                  "inviterUserId": 1,
                                  "ownerUserId": 1,
                                  "progress": "课程",
                                  "intent": "很稳",
                                  "inviterNote": "更新备注"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers/{id}", customerId).header("Authorization", bearer()))
                .andExpect(jsonPath("$.data.realName").value("测试客户A改"))
                .andExpect(jsonPath("$.data.progress").value("课程"));
    }

    @Test @Order(14)
    void shouldListCustomersWithFilters() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer())
                        .param("intent", "很稳"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ═══════════ FOLLOW-UP ═══════════

    @Test @Order(20)
    void shouldCreateFollowUp() throws Exception {
        mockMvc.perform(post("/api/follow-ups")
                        .header("Authorization", bearer())
                        .contentType(APPLICATION_JSON)
                        .content(("""
                                {
                                  "customerId": %d,
                                  "progress": "职规",
                                  "content": "深入沟通了课程需求",
                                  "feedback": "对进阶课程感兴趣",
                                  "nextAction": "发课程大纲"
                                }
                                """).formatted(customerId)))
                .andExpect(status().isOk());
    }

    @Test @Order(21)
    void shouldListFollowUps() throws Exception {
        mockMvc.perform(get("/api/follow-ups")
                        .header("Authorization", bearer())
                        .param("customerId", String.valueOf(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test @Order(22)
    void shouldFailFollowUpForNonExistentCustomer() throws Exception {
        mockMvc.perform(post("/api/follow-ups")
                        .header("Authorization", bearer())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"customerId": 99999, "progress": "职规", "content": "x"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(4004));
    }

    // ═══════════ TASK (disabled — BE-104 哲涛交付后启用) ═══════════

    @Test @Order(30)
    @org.junit.jupiter.api.Disabled("BE-104 公共安排 P1 改造未完成，待哲涛交付后启用")
    void shouldCreateTask() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "联调首页看板",
                                  "taskType": "DASHBOARD",
                                  "priority": "HIGH",
                                  "ownerUserId": 1,
                                  "dueAt": "2026-05-25T18:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        taskId = JsonTestUtils.readJsonPathAsLong(
                result.getResponse().getContentAsString(), "$.data.id");
    }

    @org.junit.jupiter.api.Disabled("BE-104 公共安排 P1 改造未完成，待哲涛交付后启用")
    @Test @Order(31)
    void shouldListTaskBoard() throws Exception {
        mockMvc.perform(get("/api/tasks/board").header("Authorization", bearer()))
                .andExpect(status().isOk());
    }

    @org.junit.jupiter.api.Disabled("BE-104 公共安排 P1 改造未完成，待哲涛交付后启用")
    @Test @Order(32)
    void shouldUpdateTaskStatus() throws Exception {
        mockMvc.perform(put("/api/tasks/{id}", taskId).header("Authorization", bearer())
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    // ═══════════ REPORT (disabled — BE-105 哲涛交付后启用) ═══════════

    @org.junit.jupiter.api.Disabled("BE-105 日报周报 P1 方案B未完成，待哲涛交付后启用")
    @Test @Order(40)
    void shouldCreateAndListDailyReport() throws Exception {
        mockMvc.perform(get("/api/reports/daily").header("Authorization", bearer()))
                .andExpect(status().isOk());
    }

    @org.junit.jupiter.api.Disabled("BE-105 日报周报 P1 方案B未完成，待哲涛交付后启用")
    @Test @Order(41)
    void shouldRejectDuplicateDailyReport() throws Exception {
    }

    @org.junit.jupiter.api.Disabled("BE-105 日报周报 P1 方案B未完成，待哲涛交付后启用")
    @Test @Order(42)
    void shouldCreateWeeklyReport() throws Exception {
    }

    // ═══════════ SCRIPT removed (P1 攻略区替换话术库) ═══════════

    // ═══════════ FINAL DASHBOARD ═══════════

    @Test @Order(60)
    void shouldShowDashboardWithRealData() throws Exception {
        // Create high-intent customer so dashboard reflects it
        mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearer())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "高意向客户B",
                                  "grade": "大一",
                                  "major": "计算机科学",
                                  "className": "计科2403",
                                  "inviterUserId": 1,
                                  "ownerUserId": 1,
                                  "progress": "技术栈",
                                  "intent": "很稳",
                                  "inviterNote": ""
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dashboard/overview").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayTasks").isArray())
                .andExpect(jsonPath("$.data.highIntentCustomerCount", greaterThan(0)));
    }
}
