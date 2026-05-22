package com.yousells.modules.task.convert;

import com.yousells.modules.task.dto.TaskCreateRequest;
import com.yousells.modules.task.entity.TaskBoardEntity;

public final class TaskBoardConvert {

    private TaskBoardConvert() {
    }

    public static TaskBoardEntity toEntity(TaskCreateRequest request, Long creatorUserId) {
        TaskBoardEntity entity = new TaskBoardEntity();
        entity.setTaskTitle(request.taskTitle());
        entity.setTaskDescription(request.taskDescription());
        entity.setDirection(request.direction());
        entity.setOwnerUserId(request.ownerUserId());
        entity.setCreatorUserId(creatorUserId);
        entity.setSuggestedToUserId(request.suggestedToUserId());
        entity.setStatus("待开始");
        entity.setPriority(request.priority() != null ? request.priority() : "中");
        entity.setDueAt(request.dueAt());
        return entity;
    }
}
