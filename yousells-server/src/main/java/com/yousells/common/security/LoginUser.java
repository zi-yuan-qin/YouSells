package com.yousells.common.security;

import java.io.Serializable;

public record LoginUser(
        Long userId,
        String username,
        String realName,
        String level,
        Long managerUserId
) implements Serializable {
}
