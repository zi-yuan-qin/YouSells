package com.yousells.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yousells.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class UserEntity extends BaseEntity {

    private String username;

    private String passwordHash;

    private String realName;

    private String level;

    private Long managerUserId;

    private String status;
}
