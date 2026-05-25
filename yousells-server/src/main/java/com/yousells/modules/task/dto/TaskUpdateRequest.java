package com.yousells.modules.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskUpdateRequest(
        @NotBlank(message = "taskTitle cannot be blank")
        String taskTitle,
        @NotBlank(message = "status cannot be blank")
        String status,
        String taskDescription,
        String priority,
        @NotNull(message = "ownerUserId cannot be null")
        Long ownerUserId,
        LocalDateTime dueAt
) {
}
