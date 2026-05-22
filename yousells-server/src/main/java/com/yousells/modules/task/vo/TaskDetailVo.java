package com.yousells.modules.task.vo;
import java.time.LocalDateTime;
public record TaskDetailVo(
        Long id, String taskTitle, String taskDescription, String direction, String status,
        String priority, String ownerDisplayName, String creatorDisplayName,
        String suggestedToDisplayName, LocalDateTime dueAt,
        Long ownerUserId, Long creatorUserId, Long suggestedToUserId) {}
