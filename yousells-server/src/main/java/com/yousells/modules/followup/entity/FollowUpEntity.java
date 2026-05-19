package com.yousells.modules.followup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_follow_ups")
public class FollowUpEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private String followType;

    private String communicatedContent;

    private String customerFeedback;

    private String currentConcern;

    private String nextAction;

    private LocalDateTime nextFollowAt;

    private Long operatorUserId;

    private Long ownerUserId;

    private String stageBefore;

    private String stageAfter;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;
}
