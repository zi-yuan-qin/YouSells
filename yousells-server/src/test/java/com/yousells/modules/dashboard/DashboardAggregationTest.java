package com.yousells.modules.dashboard;

import com.yousells.common.response.PageResponse;
import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import com.yousells.modules.dashboard.service.impl.DashboardServiceImpl;
import com.yousells.modules.dashboard.vo.DashboardOverviewVo;
import com.yousells.modules.followup.dto.FollowUpQueryRequest;
import com.yousells.modules.followup.service.FollowUpService;
import com.yousells.modules.followup.vo.FollowUpVo;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.service.TaskBoardService;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardAggregationTest {

    @Test
    void shouldAggregateOverviewFromCustomerFollowUpAndTaskModules() {
        CustomerService customerService = mock(CustomerService.class);
        FollowUpService followUpService = mock(FollowUpService.class);
        TaskBoardService taskBoardService = mock(TaskBoardService.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-19T04:00:00Z"), ZoneId.of("Asia/Hong_Kong"));

        when(customerService.pageCustomers(any(CustomerQueryRequest.class))).thenReturn(PageResponse.of(List.of(
                new CustomerListItemVo(1001L, "C20260518001", "小林", "OLD_GRADE1", "QQ", "A", "HIGH_INTENT", "秦梓源",
                        LocalDateTime.of(2026, 5, 17, 21, 30), LocalDateTime.of(2026, 5, 19, 12, 0), List.of("竞赛意向")),
                new CustomerListItemVo(1002L, "C20260518002", "阿泽", "OLD_GRADE1", "微信", "B", "NURTURING", "志明",
                        LocalDateTime.of(2026, 5, 18, 10, 0), LocalDateTime.of(2026, 5, 20, 20, 0), List.of("零基础")),
                new CustomerListItemVo(1003L, "C20260519003", "阿宁", "OLD_GRADE1", "QQ", "C", "FIRST_COMMUNICATION", "哲涛",
                        LocalDateTime.of(2026, 5, 16, 19, 45), LocalDateTime.of(2026, 5, 18, 18, 0), List.of("想进群")),
                new CustomerListItemVo(1004L, "C20260510004", "阿青", "OLD_GRADE1", "QQ", "A", "PENDING_CLOSE", "嘉诚",
                        LocalDateTime.of(2026, 5, 15, 9, 0), LocalDateTime.of(2026, 5, 19, 9, 0), List.of("高意向")),
                new CustomerListItemVo(1005L, "C20260508005", "小周", "OLD_GRADE1", "QQ", "A", "TRANSFER_TO_EXAM", "许润",
                        LocalDateTime.of(2026, 5, 15, 9, 0), LocalDateTime.of(2026, 5, 19, 8, 0), List.of("转考研"))
        ), 1, 1000, 5));

        when(followUpService.pageFollowUps(any(FollowUpQueryRequest.class))).thenReturn(PageResponse.of(List.of(
                new FollowUpVo(5001L, 1001L, "CHAT", "聊了学习路线", "愿意继续了解", "担心时间安排", "发项目路线图",
                        LocalDateTime.of(2026, 5, 19, 12, 0), "秦梓源", "秦梓源", LocalDateTime.of(2026, 5, 17, 21, 30)),
                new FollowUpVo(5002L, 1001L, "GROUP", "拉进技术群", "对群内项目感兴趣", "担心基础弱", "安排简单体验",
                        LocalDateTime.of(2026, 5, 20, 19, 0), "志明", "秦梓源", LocalDateTime.of(2026, 5, 18, 10, 10)),
                new FollowUpVo(5003L, 1003L, "CHAT", "发了课程结构", "先看看案例", "价格", "补项目案例",
                        LocalDateTime.of(2026, 5, 18, 20, 0), "志明", "志明", LocalDateTime.of(2026, 5, 18, 20, 30)),
                new FollowUpVo(5004L, 1004L, "CHAT", "确认最后疑问", "准备再约一次", "付款流程", "跟进成交",
                        LocalDateTime.of(2026, 5, 19, 18, 0), "嘉诚", "嘉诚", LocalDateTime.of(2026, 5, 18, 22, 0))
        ), 1, 1000, 4));

        when(taskBoardService.pageTasks(any(TaskQueryRequest.class))).thenReturn(PageResponse.of(List.of(
                new TaskBoardItemVo(301L, "整理高意向客户回访清单", "CUSTOMER", "IN_PROGRESS", "HIGH", "秦梓源", "志明", null, LocalDateTime.of(2026, 5, 19, 20, 0)),
                new TaskBoardItemVo(302L, "补齐日报模板字段说明", "REPORT", "TODO", "MEDIUM", "志明", null, null, LocalDateTime.of(2026, 5, 19, 22, 0)),
                new TaskBoardItemVo(303L, "整理话术库首版分类", "SCRIPT", "DONE", "MEDIUM", "哲涛", "许润", null, LocalDateTime.of(2026, 5, 17, 18, 0)),
                new TaskBoardItemVo(304L, "确认小镇Coders海报文案", "GROUP", "TODO", "HIGH", "嘉诚", "许润", null, LocalDateTime.of(2026, 5, 18, 16, 0)),
                new TaskBoardItemVo(305L, "联调首页看板字段", "DASHBOARD", "BLOCKED", "HIGH", "秦梓源", "嘉诚", null, null)
        ), 1, 1000, 5));

        DashboardServiceImpl dashboardService = new DashboardServiceImpl(
                customerService,
                followUpService,
                taskBoardService,
                fixedClock
        );

        DashboardOverviewVo overview = dashboardService.getOverview();

        assertEquals(3, overview.todayPendingFollowCount());
        assertEquals(2, overview.overdueCustomerCount());
        assertEquals(3, overview.recentNewCustomerCount());
        assertEquals(2, overview.highIntentCustomerCount());
        assertEquals(4, overview.todayTasks().size());
        assertIterableEquals(List.of(304L, 301L, 302L, 305L),
                overview.todayTasks().stream().map(item -> item.taskId()).toList());
        assertEquals(4, overview.focusCustomers().size());
        assertIterableEquals(List.of(1004L, 1001L, 1003L, 1002L),
                overview.focusCustomers().stream().map(item -> item.customerId()).toList());
    }
}
