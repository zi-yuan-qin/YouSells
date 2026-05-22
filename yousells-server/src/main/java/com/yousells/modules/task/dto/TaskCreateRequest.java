package com.yousells.modules.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskCreateRequest(
        @NotBlank(message = "taskTitle cannot be blank")
        String taskTitle,

        String taskDescription,

        @NotBlank(message = "direction cannot be blank")
        String direction,

        @NotNull(message = "ownerUserId cannot be null")
        Long ownerUserId,

        Long suggestedToUserId,

        String priority,

        LocalDateTime dueAt
) {
}
