package com.yousells.modules.task.controller;

import com.yousells.common.response.ApiResponse;
import com.yousells.common.response.IdResponse;
import com.yousells.common.response.PageResponse;
import com.yousells.modules.task.dto.TaskCreateRequest;
import com.yousells.modules.task.dto.TaskLogCreateRequest;
import com.yousells.modules.task.dto.TaskQueryRequest;
import com.yousells.modules.task.dto.TaskStatusUpdateRequest;
import com.yousells.modules.task.service.TaskBoardService;
import com.yousells.modules.task.vo.TaskBoardColumnVo;
import com.yousells.modules.task.vo.TaskBoardItemVo;
import com.yousells.modules.task.vo.TaskDetailWithLogsVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskBoardService taskBoardService;

    public TaskController(TaskBoardService taskBoardService) {
        this.taskBoardService = taskBoardService;
    }

    @GetMapping
    public ApiResponse<PageResponse<TaskBoardItemVo>> page(TaskQueryRequest request) {
        return ApiResponse.success(taskBoardService.pageTasks(request));
    }

    @GetMapping("/board")
    public ApiResponse<List<TaskBoardColumnVo>> board() {
        return ApiResponse.success(taskBoardService.listBoard());
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskDetailWithLogsVo> detail(@PathVariable Long id) {
        return ApiResponse.success(taskBoardService.getTask(id));
    }

    @PostMapping
    public ApiResponse<IdResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.success(new IdResponse(taskBoardService.createTask(request)));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody TaskStatusUpdateRequest request) {
        taskBoardService.updateTaskStatus(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/logs")
    public ApiResponse<Void> addLog(@PathVariable Long id,
                                    @Valid @RequestBody TaskLogCreateRequest request) {
        taskBoardService.addTaskLog(id, request);
        return ApiResponse.success();
    }
}
