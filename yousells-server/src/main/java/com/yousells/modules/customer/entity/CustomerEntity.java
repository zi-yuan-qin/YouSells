package com.yousells.modules.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customers")
public class CustomerEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String customerCode;

    private String customerType;

    private String nickname;

    private String contactValue;

    private String sourcePlatform;

    private LocalDateTime addedAt;

    private Integer isNuistFreshman;

    private String expectedMajor;

    private String baseLevel;

    private String interestDirection;

    private String intentLevel;

    private String currentStage;

    private String currentConcern;

    private String latestFeedback;

    private LocalDateTime lastContactAt;

    private String nextFollowAction;

    private LocalDateTime nextFollowAt;

    private Long ownerUserId;

    private Long assistantUserId;

    private Integer needsSupport;

    private String conversionResult;

    private String remarks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdBy;

    private Long updatedBy;

    private Integer isDeleted;
}
