package com.yousells.modules.dashboard;

import com.yousells.common.response.PageResponse;
import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import com.yousells.modules.dashboard.service.impl.DashboardServiceImpl;
import com.yousells.modules.dashboard.vo.DashboardOverviewVo;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardAggregationTest {

    @Test
    void shouldAggregateOverviewFromCustomerAndTaskModules() {
        CustomerService customerService = mock(CustomerService.class);
        TaskBoardService taskBoardService = mock(TaskBoardService.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-19T04:00:00Z"), ZoneId.of("Asia/Shanghai"));

        when(customerService.pageCustomers(any(CustomerQueryRequest.class))).thenReturn(PageResponse.of(List.of(
                new CustomerListItemVo(1001L, "王同学", "大一", "计算机科学", "计科2403", "技术栈", "可跟", "秦梓源", "小赵",
                        LocalDateTime.of(2026, 5, 18, 12, 0)),
                new CustomerListItemVo(1002L, "张同学", "大二", "软件工程", "软工2301", "课程", "很稳", "秦梓源", "秦梓源",
                        LocalDateTime.of(2026, 5, 17, 10, 0)),
                new CustomerListItemVo(1003L, "李同学", "大一", "数学应用", null, "职规", "观望", "秦梓源", "小赵",
                        LocalDateTime.of(2026, 5, 15, 9, 0))
        ), 1, 1000, 3));

        when(taskBoardService.pageTasks(any(TaskQueryRequest.class))).thenReturn(PageResponse.of(List.of(), 1, 1000, 0));

        DashboardServiceImpl dashboardService = new DashboardServiceImpl(
                customerService, taskBoardService, fixedClock);

        DashboardOverviewVo overview = dashboardService.getOverview();

        assertEquals(2, overview.todayPendingFollowCount());
        assertEquals(0, overview.overdueCustomerCount());
        assertEquals(3, overview.recentNewCustomerCount());
        assertEquals(2, overview.highIntentCustomerCount());
        assertEquals(2, overview.focusCustomers().size());
    }
}
