package com.yousells.modules.task.vo;
import java.time.LocalDateTime;
public record TaskLogVo(
        Long id, Long taskId, Long userId, String userRealName, String type,
        String content, String fromStatus, String toStatus, LocalDateTime createdAt) {}
