package com.yousells.modules.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@TableName("daily_reports")
public class DailyReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("report_date")
    private LocalDate reportDate;

    @TableField("user_id")
    private Long userId;

    @TableField("summary")
    private String summary;

    @TableField("issues")
    private String issues;

    @TableField("tomorrow_plan")
    private String tomorrowPlan;

    @TableField("new_customer_count")
    private Integer newCustomerCount;

    @TableField("follow_up_count")
    private Integer followUpCount;

    @TableField("progress_advance_count")
    private Integer progressAdvanceCount;

    @TableField("task_completed_count")
    private Integer taskCompletedCount;

    @TableField("progress_details")
    private String progressDetails;

    @TableField("task_completed_details")
    private String taskCompletedDetails;

    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Integer isDeleted;
}
