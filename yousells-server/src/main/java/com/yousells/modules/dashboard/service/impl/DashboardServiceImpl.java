package com.yousells.modules.dashboard.service.impl;

import com.yousells.common.constant.CustomerStageConstants;
import com.yousells.common.constant.IntentLevelConstants;
import com.yousells.common.constant.TaskStatusConstants;
import com.yousells.modules.customer.dto.CustomerQueryRequest;
import com.yousells.modules.customer.service.CustomerService;
import com.yousells.modules.customer.vo.CustomerListItemVo;
import com.yousells.modules.dashboard.service.DashboardService;
import com.yousells.modules.dashboard.vo.DashboardCustomerReminderVo;
import com.yousells.modules.dashboard.vo.DashboardOverviewVo;
import com.yousells.modules.dashboard.vo.DashboardTaskReminderVo;
import com.yousells.modules.followup.dto.FollowUpQueryRequest;
import com.yousells.modules.followup.service.FollowUpService;
import com.yousells.modules.followup.vo.FollowUpVo;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.service.TaskBoardService;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int DASHBOARD_FETCH_SIZE = 1000;
    private static final int TASK_REMINDER_LIMIT = 5;
    private static final int FOCUS_CUSTOMER_LIMIT = 5;
    private static final int RECENT_CUSTOMER_DAYS = 7;
    private static final Pattern CUSTOMER_CODE_DATE_PATTERN = Pattern.compile("^C(\\d{8})\\d*$");

    private final CustomerService customerService;
    private final FollowUpService followUpService;
    private final TaskBoardService taskBoardService;
    private final Clock clock;

    public DashboardServiceImpl(CustomerService customerService,
                                FollowUpService followUpService,
                                TaskBoardService taskBoardService,
                                Clock clock) {
        this.customerService = customerService;
        this.followUpService = followUpService;
        this.taskBoardService = taskBoardService;
        this.clock = clock;
    }

    @Override
    public DashboardOverviewVo getOverview() {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        List<CustomerListItemVo> customers = fetchCustomers();
        List<FollowUpVo> followUps = fetchFollowUps();
        List<TaskBoardItemVo> tasks = fetchTasks();

        return new DashboardOverviewVo(
                countTodayPendingFollows(customers, followUps, today),
                countOverdueCustomers(customers, followUps, now),
                countRecentCustomers(customers, today),
                countHighIntentCustomers(customers),
                buildTaskReminders(tasks),
                buildFocusCustomers(customers, followUps, now)
        );
    }

    private List<CustomerListItemVo> fetchCustomers() {
        return customerService.pageCustomers(new CustomerQueryRequest(1, DASHBOARD_FETCH_SIZE, null, null, null, null, null)).list();
    }

    private List<FollowUpVo> fetchFollowUps() {
        return followUpService.pageFollowUps(new FollowUpQueryRequest(null, 1, DASHBOARD_FETCH_SIZE)).list();
    }

    private List<TaskBoardItemVo> fetchTasks() {
        return taskBoardService.pageTasks(new TaskQueryRequest(1, DASHBOARD_FETCH_SIZE, null, null, null)).list();
    }

    private int countTodayPendingFollows(List<CustomerListItemVo> customers, List<FollowUpVo> followUps, LocalDate today) {
        return (int) customers.stream()
                .filter(this::isActiveCustomer)
                .map(customer -> resolvePendingFollowAt(customer, followUps))
                .filter(nextFollowAt -> nextFollowAt != null)
                .filter(nextFollowAt -> !nextFollowAt.toLocalDate().isAfter(today))
                .count();
    }

    private int countOverdueCustomers(List<CustomerListItemVo> customers, List<FollowUpVo> followUps, LocalDateTime now) {
        return (int) customers.stream()
                .filter(this::isActiveCustomer)
                .map(customer -> resolvePendingFollowAt(customer, followUps))
                .filter(nextFollowAt -> nextFollowAt != null)
                .filter(nextFollowAt -> nextFollowAt.isBefore(now))
                .count();
    }

    private int countRecentCustomers(List<CustomerListItemVo> customers, LocalDate today) {
        LocalDate windowStart = today.minusDays(RECENT_CUSTOMER_DAYS - 1L);
        return (int) customers.stream()
                .map(CustomerListItemVo::customerCode)
                .map(this::extractCreatedDate)
                .filter(createdDate -> createdDate != null)
                .filter(createdDate -> !createdDate.isBefore(windowStart) && !createdDate.isAfter(today))
                .count();
    }

    private int countHighIntentCustomers(List<CustomerListItemVo> customers) {
        return (int) customers.stream()
                .filter(this::isActiveCustomer)
                .filter(this::isHighIntentCustomer)
                .count();
    }

    private List<DashboardTaskReminderVo> buildTaskReminders(List<TaskBoardItemVo> tasks) {
        return tasks.stream()
                .filter(task -> !TaskStatusConstants.DONE.equals(task.status()))
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

    private List<DashboardCustomerReminderVo> buildFocusCustomers(List<CustomerListItemVo> customers,
                                                                  List<FollowUpVo> followUps,
                                                                  LocalDateTime now) {
        return customers.stream()
                .filter(this::isActiveCustomer)
                .sorted(Comparator
                        .comparing((CustomerListItemVo customer) -> isHighIntentCustomer(customer) ? 0 : 1)
                        .thenComparing(customer -> {
                            LocalDateTime nextFollowAt = resolvePendingFollowAt(customer, followUps);
                            return nextFollowAt != null && nextFollowAt.isBefore(now) ? 0 : 1;
                        })
                        .thenComparing(customer -> resolvePendingFollowAt(customer, followUps), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CustomerListItemVo::nickname, Comparator.nullsLast(String::compareTo)))
                .filter(customer -> resolvePendingFollowAt(customer, followUps) != null)
                .limit(FOCUS_CUSTOMER_LIMIT)
                .map(customer -> new DashboardCustomerReminderVo(
                        customer.id(),
                        customer.nickname(),
                        customer.intentLevel(),
                        customer.currentStage(),
                        resolvePendingFollowAt(customer, followUps)
                ))
                .toList();
    }

    private LocalDateTime resolvePendingFollowAt(CustomerListItemVo customer, List<FollowUpVo> followUps) {
        if (customer.nextFollowAt() != null) {
            return customer.nextFollowAt();
        }

        return followUps.stream()
                .filter(followUp -> customer.id().equals(followUp.customerId()))
                .map(FollowUpVo::nextFollowAt)
                .filter(nextFollowAt -> nextFollowAt != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private boolean isHighIntentCustomer(CustomerListItemVo customer) {
        return IntentLevelConstants.A.equals(customer.intentLevel())
                || CustomerStageConstants.HIGH_INTENT.equals(customer.currentStage())
                || CustomerStageConstants.PENDING_CLOSE.equals(customer.currentStage());
    }

    private boolean isActiveCustomer(CustomerListItemVo customer) {
        return !CustomerStageConstants.CONVERTED.equals(customer.currentStage())
                && !CustomerStageConstants.PAUSED.equals(customer.currentStage())
                && !CustomerStageConstants.TRANSFER_TO_EXAM.equals(customer.currentStage());
    }

    // P0 阶段还没有正式 createdAt 字段，这里先用客户编码中的日期段做最近新增统计。
    private LocalDate extractCreatedDate(String customerCode) {
        if (customerCode == null || customerCode.isBlank()) {
            return null;
        }

        Matcher matcher = CUSTOMER_CODE_DATE_PATTERN.matcher(customerCode);
        if (!matcher.matches()) {
            return null;
        }

        try {
            return LocalDate.parse(matcher.group(1), DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
