package com.yousells.modules.task.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.DataScopeHelper;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.task.convert.TaskBoardConvert;
import com.yousells.modules.task.dto.TaskCreateRequest;
import com.yousells.modules.task.dto.TaskLogCreateRequest;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.dto.TaskStatusUpdateRequest;
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

    public TaskBoardServiceImpl(TaskBoardMapper taskBoardMapper,
                                TaskLogMapper taskLogMapper,
                                UserMapper userMapper) {
        this.taskBoardMapper = taskBoardMapper;
        this.taskLogMapper = taskLogMapper;
        this.userMapper = userMapper;
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
        List<TaskLogVo> logs = taskLogMapper.selectByTaskId(id);
        return new TaskDetailWithLogsVo(detail, logs);
    }

    @Override
    public Long createTask(TaskCreateRequest request) {
        LoginUser user = SecurityUserContext.requireCurrentUser();
        TaskBoardEntity entity = TaskBoardConvert.toEntity(request, user.userId());
        taskBoardMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateTaskStatus(Long id, TaskStatusUpdateRequest request) {
        TaskBoardEntity entity = taskBoardMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "task not found");
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
    }

    @Override
    public void addTaskLog(Long id, TaskLogCreateRequest request) {
        TaskBoardEntity entity = taskBoardMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "task not found");
        }
        LoginUser user = SecurityUserContext.requireCurrentUser();
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
