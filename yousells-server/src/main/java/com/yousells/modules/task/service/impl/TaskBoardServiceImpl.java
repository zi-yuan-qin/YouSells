package com.yousells.modules.task.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.DataScopeHelper;
import com.yousells.common.constant.NotificationTypeConstants;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.notification.entity.NotificationEntity;
import com.yousells.modules.notification.service.NotificationService;
import com.yousells.modules.task.convert.TaskBoardConvert;
import com.yousells.modules.task.dto.TaskCreateRequest;
import com.yousells.modules.task.dto.TaskLogCreateRequest;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.dto.TaskStatusUpdateRequest;
import com.yousells.modules.task.dto.TaskUpdateRequest;
import com.yousells.modules.task.entity.TaskBoardEntity;
import com.yousells.modules.task.entity.TaskLogEntity;
import com.yousells.modules.task.mapper.TaskBoardMapper;
import com.yousells.modules.task.mapper.TaskLogMapper;
import com.yousells.modules.task.service.TaskBoardService;
import com.yousells.modules.task.vo.TaskBoardColumnVo;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import com.yousells.modules.task.vo.TaskDetailVo;
import com.yousells.modules.task.vo.TaskDetailWithLogsVo;
import com.yousells.modules.task.vo.TaskLogVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskBoardServiceImpl implements TaskBoardService {

    private static final Map<String, String> STATUS_TITLE_MAP = new LinkedHashMap<>();

    static {
        STATUS_TITLE_MAP.put("待开始", "待开始");
        STATUS_TITLE_MAP.put("进行中", "进行中");
        STATUS_TITLE_MAP.put("已完成", "已完成");
    }

    private final TaskBoardMapper taskBoardMapper;
    private final TaskLogMapper taskLogMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public TaskBoardServiceImpl(TaskBoardMapper taskBoardMapper,
                                TaskLogMapper taskLogMapper,
                                UserMapper userMapper,
                                NotificationService notificationService) {
        this.taskBoardMapper = taskBoardMapper;
        this.taskLogMapper = taskLogMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    public PageResponse<TaskBoardItemVo> pageTasks(TaskQueryRequest request) {
        int page = request.page() == null || request.page() < 1 ? 1 : request.page();
        int pageSize = request.pageSize() == null || request.pageSize() < 1 ? 20 : request.pageSize();
        List<Long> visibleOwnerIds = resolveVisibleOwnerIds();
        IPage<TaskBoardItemVo> result = taskBoardMapper.selectPageWithUsers(
                Page.of(page, pageSize),
                request.status(),
                request.ownerUserId(),
                request.direction(),
                visibleOwnerIds
        );
        return PageResponse.of(result.getRecords(), page, pageSize, result.getTotal());
    }

    @Override
    public List<TaskBoardColumnVo> listBoard() {
        List<Long> visibleOwnerIds = resolveVisibleOwnerIds();
        List<TaskBoardItemVo> allTasks = taskBoardMapper.selectAllWithUsers(visibleOwnerIds);

        return STATUS_TITLE_MAP.entrySet().stream()
                .map(entry -> {
                    List<TaskBoardItemVo> items = allTasks.stream()
                            .filter(task -> entry.getKey().equals(task.status()))
                            .toList();
                    return new TaskBoardColumnVo(entry.getKey(), entry.getValue(), items);
                })
                .toList();
    }

    @Override
    public TaskDetailWithLogsVo getTask(Long id) {
        TaskDetailVo detail = taskBoardMapper.selectDetailById(id);
        if (detail == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "task not found");
        }
        List<Long> visibleOwnerIds = resolveVisibleOwnerIds();
        LoginUser currentUser = SecurityUserContext.requireCurrentUser();
        if (!visibleOwnerIds.contains(detail.ownerUserId()) && !currentUser.userId().equals(detail.creatorUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权查看该任务");
        }
        List<TaskLogVo> logs = taskLogMapper.selectByTaskId(id);
        return new TaskDetailWithLogsVo(detail, logs);
    }

    @Override
    @Transactional
    public Long createTask(TaskCreateRequest request) {
        LoginUser user = SecurityUserContext.requireCurrentUser();
        List<Long> visibleUserIds = resolveVisibleOwnerIds();
        if (!visibleUserIds.contains(request.ownerUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "指定的执行人不在可操作范围内");
        }
        TaskBoardEntity entity = TaskBoardConvert.toEntity(request, user.userId());
        taskBoardMapper.insert(entity);

        // 通知任务执行人
        if (!entity.getOwnerUserId().equals(user.userId())) {
            NotificationEntity notification = new NotificationEntity();
            notification.setUserId(entity.getOwnerUserId());
            notification.setType(NotificationTypeConstants.TASK_ASSIGNED);
            notification.setTitle("新任务指派");
            notification.setContent("「" + user.realName() + "」给你指派了新任务：" + entity.getTaskTitle());
            notification.setBusinessType("task");
            notification.setBusinessId(entity.getId());
            notificationService.sendNotification(notification);
        }

        return entity.getId();
    }

    @Override
    @Transactional
    public void updateTask(Long id, TaskUpdateRequest request) {
        TaskBoardEntity entity = taskBoardMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "task not found");
        }
        LoginUser currentUser = SecurityUserContext.requireCurrentUser();
        if (!currentUser.userId().equals(entity.getOwnerUserId()) && !currentUser.userId().equals(entity.getCreatorUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权修改该任务");
        }

        // 检查新的负责人是否在可操作范围内
        List<Long> visibleUserIds = resolveVisibleOwnerIds();
        if (!visibleUserIds.contains(request.ownerUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "指定的执行人不在可操作范围内");
        }

        String oldOwnerUserId = String.valueOf(entity.getOwnerUserId());
        entity.setTaskTitle(request.taskTitle());
        entity.setStatus(request.status());
        entity.setTaskDescription(request.taskDescription());
        entity.setPriority(request.priority());
        entity.setOwnerUserId(request.ownerUserId());
        entity.setDueAt(request.dueAt());
        taskBoardMapper.updateById(entity);

        LoginUser user = SecurityUserContext.requireCurrentUser();
        TaskLogEntity log = new TaskLogEntity();
        log.setTaskId(id);
        log.setUserId(user.userId());
        log.setType("编辑任务");
        log.setContent("更新了任务「" + entity.getTaskTitle() + "」");
        taskLogMapper.insert(log);

        // 如果负责人变更了，通知新的负责人
        if (!oldOwnerUserId.equals(String.valueOf(request.ownerUserId()))) {
            NotificationEntity notification = new NotificationEntity();
            notification.setUserId(request.ownerUserId());
            notification.setType(NotificationTypeConstants.TASK_ASSIGNED);
            notification.setTitle("任务负责人变更");
            notification.setContent("你被指定为任务「" + entity.getTaskTitle() + "」的新负责人");
            notification.setBusinessType("task");
            notification.setBusinessId(entity.getId());
            notificationService.sendNotification(notification);
        }
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long id, TaskStatusUpdateRequest request) {
        TaskBoardEntity entity = taskBoardMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "task not found");
        }
        LoginUser currentUser = SecurityUserContext.requireCurrentUser();
        if (!currentUser.userId().equals(entity.getOwnerUserId()) && !currentUser.userId().equals(entity.getCreatorUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权修改该任务状态");
        }
        String oldStatus = entity.getStatus();
        String newStatus = request.status();
        entity.setStatus(newStatus);
        taskBoardMapper.updateById(entity);

        LoginUser user = SecurityUserContext.requireCurrentUser();
        TaskLogEntity log = new TaskLogEntity();
        log.setTaskId(id);
        log.setUserId(user.userId());
        log.setType("状态变更");
        log.setFromStatus(oldStatus);
        log.setToStatus(newStatus);
        taskLogMapper.insert(log);

        // 通知任务创建人（如果创建人不是当前操作人）
        Long creatorId = entity.getCreatorUserId();
        if (creatorId != null && !creatorId.equals(user.userId())) {
            NotificationEntity notification = new NotificationEntity();
            notification.setUserId(creatorId);
            notification.setType(NotificationTypeConstants.TASK_STATUS_CHANGED);
            notification.setTitle("任务状态变更");
            notification.setContent("你创建的任务「" + entity.getTaskTitle() + "」状态从「" + oldStatus + "」变更为「" + newStatus + "」");
            notification.setBusinessType("task");
            notification.setBusinessId(entity.getId());
            notificationService.sendNotification(notification);
        }
    }

    @Override
    public void addTaskLog(Long id, TaskLogCreateRequest request) {
        TaskBoardEntity entity = taskBoardMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "task not found");
        }
        LoginUser user = SecurityUserContext.requireCurrentUser();
        if (!user.userId().equals(entity.getOwnerUserId()) && !user.userId().equals(entity.getCreatorUserId())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权操作该任务");
        }
        TaskLogEntity log = new TaskLogEntity();
        log.setTaskId(id);
        log.setUserId(user.userId());
        log.setType(request.type());
        log.setContent(request.content());
        taskLogMapper.insert(log);
    }

    private List<Long> resolveVisibleOwnerIds() {
        LoginUser user = SecurityUserContext.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        ids.add(user.userId());
        ids.addAll(DataScopeHelper.getSubordinateIds(user.userId(), userMapper));
        return ids;
    }
}
