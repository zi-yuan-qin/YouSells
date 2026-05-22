package com.yousells.modules.task.dto;
import jakarta.validation.constraints.NotBlank;
public record TaskStatusUpdateRequest(@NotBlank(message = "status cannot be blank") String status) {}
