package com.yousells.modules.task.dto;
import jakarta.validation.constraints.NotBlank;
public record TaskLogCreateRequest(
        @NotBlank(message = "type cannot be blank") String type,
        @NotBlank(message = "content cannot be blank") String content) {}
