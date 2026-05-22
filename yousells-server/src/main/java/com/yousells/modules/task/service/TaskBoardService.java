package com.yousells.modules.task.service;

import com.yousells.common.response.PageResponse;
import com.yousells.modules.task.dto.TaskCreateRequest;
import com.yousells.modules.task.dto.TaskLogCreateRequest;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.dto.TaskStatusUpdateRequest;
import com.yousells.modules.task.vo.TaskBoardColumnVo;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import com.yousells.modules.task.vo.TaskDetailWithLogsVo;

import java.util.List;

public interface TaskBoardService {

    PageResponse<TaskBoardItemVo> pageTasks(TaskQueryRequest request);

    List<TaskBoardColumnVo> listBoard();

    TaskDetailWithLogsVo getTask(Long id);

    Long createTask(TaskCreateRequest request);

    void updateTaskStatus(Long id, TaskStatusUpdateRequest request);

    void addTaskLog(Long id, TaskLogCreateRequest request);
}
