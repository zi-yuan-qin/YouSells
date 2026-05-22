package com.yousells.modules.dashboard.service.impl;

import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import com.yousells.modules.dashboard.service.DashboardService;
import com.yousells.modules.dashboard.vo.DashboardCustomerReminderVo;
import com.yousells.modules.dashboard.vo.DashboardOverviewVo;
import com.yousells.modules.dashboard.vo.DashboardTaskReminderVo;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.service.TaskBoardService;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int DASHBOARD_FETCH_SIZE = 1000;
    private static final int TASK_REMINDER_LIMIT = 5;
    private static final int FOCUS_CUSTOMER_LIMIT = 5;
    private static final int RECENT_CUSTOMER_DAYS = 7;

    private final CustomerService customerService;
    private final TaskBoardService taskBoardService;
    private final Clock clock;

    public DashboardServiceImpl(CustomerService customerService,
                                TaskBoardService taskBoardService,
                                Clock clock) {
        this.customerService = customerService;
        this.taskBoardService = taskBoardService;
        this.clock = clock;
    }

    @Override
    public DashboardOverviewVo getOverview() {
        LocalDate today = LocalDate.now(clock);
        List<CustomerListItemVo> customers = fetchCustomers();
        List<TaskBoardItemVo> tasks = fetchTasks();

        return new DashboardOverviewVo(
                countTodayPendingFollows(customers),
                countOverdueCustomers(customers),
                countRecentCustomers(customers, today),
                countHighIntentCustomers(customers),
                buildTaskReminders(tasks),
                buildFocusCustomers(customers)
        );
    }

    private List<CustomerListItemVo> fetchCustomers() {
        return customerService.pageCustomers(
                new CustomerQueryRequest(null, null, null, null, null, null, 1, DASHBOARD_FETCH_SIZE)).list();
    }

    private List<TaskBoardItemVo> fetchTasks() {
        return taskBoardService.pageTasks(new TaskQueryRequest(1, DASHBOARD_FETCH_SIZE, null)).list();
    }

    private int countTodayPendingFollows(List<CustomerListItemVo> customers) {
        // P1: 待跟进 = 职规/技术栈阶段的活跃客户（BE-107 收口后通过跟进记录精确计算）
        return (int) customers.stream()
                .filter(c -> !"课程".equals(c.progress()))
                .count();
    }

    private int countOverdueCustomers(List<CustomerListItemVo> customers) {
        // P1: 待 BE-104 任务日志就绪后通过跟进记录 lastAction 精确计算
        return 0;
    }

    private int countRecentCustomers(List<CustomerListItemVo> customers, LocalDate today) {
        LocalDate windowStart = today.minusDays(RECENT_CUSTOMER_DAYS - 1L);
        return (int) customers.stream()
                .map(CustomerListItemVo::createdAt)
                .filter(createdAt -> createdAt != null)
                .filter(createdAt -> {
                    LocalDate createdDate = createdAt.toLocalDate();
                    return !createdDate.isBefore(windowStart) && !createdDate.isAfter(today);
                })
                .count();
    }

    private int countHighIntentCustomers(List<CustomerListItemVo> customers) {
        return (int) customers.stream()
                .filter(c -> "很稳".equals(c.intent()) || "可跟".equals(c.intent()))
                .count();
    }

    private List<DashboardTaskReminderVo> buildTaskReminders(List<TaskBoardItemVo> tasks) {
        return tasks.stream()
                .filter(task -> !"已完成".equals(task.status()))
                .sorted(Comparator.comparing(TaskBoardItemVo::dueAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(TASK_REMINDER_LIMIT)
                .map(task -> new DashboardTaskReminderVo(
                        task.id(),
                        task.taskTitle(),
                        task.status(),
                        task.ownerDisplayName(),
                        task.dueAt()
                ))
                .toList();
    }

    private List<DashboardCustomerReminderVo> buildFocusCustomers(List<CustomerListItemVo> customers) {
        return customers.stream()
                .filter(c -> "很稳".equals(c.intent()) || "可跟".equals(c.intent()))
                .limit(FOCUS_CUSTOMER_LIMIT)
                .map(customer -> new DashboardCustomerReminderVo(
                        customer.id(),
                        customer.realName(),
                        customer.intent(),
                        customer.progress(),
                        null
                ))
                .toList();
    }
}
