package com.yousells.modules.task.vo;
import java.util.List;
public record TaskDetailWithLogsVo(TaskDetailVo task, List<TaskLogVo> logs) {}
