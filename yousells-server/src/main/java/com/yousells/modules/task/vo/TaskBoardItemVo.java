package com.yousells.modules.task.vo;

import java.time.LocalDateTime;

public record TaskBoardItemVo(
        Long id,
        String taskTitle,
        String direction,
        String status,
        String priority,
        String ownerDisplayName,
        String creatorDisplayName,
        String suggestedToDisplayName,
        LocalDateTime dueAt
) {
}
