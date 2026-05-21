package com.yousells.modules.auth.vo;

public record CurrentUserVo(
        Long userId,
        String username,
        String realName,
        String level,
        Long managerUserId
) {
}
