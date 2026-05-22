package com.yousells.modules.task.dto;

public record TaskQueryRequest(
        Integer page,
        Integer pageSize,
        String status,
        Long ownerUserId,
        String direction
) {
}
