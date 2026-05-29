package com.yousells.modules.customer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yousells.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_churn_risk")
public class ChurnRiskEntity extends BaseEntity {

    private Long customerId;

    private String riskLevel;

    private Integer riskScore;

    private String riskFactors;

    private String suggestion;

    private LocalDateTime evaluatedAt;

    @TableField(exist = false)
    private Long createdBy;

    @TableField(exist = false)
    private Long updatedBy;

    @TableField(exist = false)
    private Integer isDeleted;
}
