package com.yousells.modules.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@TableName("weekly_reports")
public class WeeklyReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("week_key")
    private String weekKey;

    @TableField("user_id")
    private Long userId;

    @TableField("summary")
    private String summary;

    @TableField("issues")
    private String issues;

    @TableField("next_week_plan")
    private String nextWeekPlan;

    @TableField("week_start")
    private LocalDate weekStart;

    @TableField("week_end")
    private LocalDate weekEnd;

    @TableField("new_customer_count")
    private Integer newCustomerCount;

    @TableField("follow_up_count")
    private Integer followUpCount;

    @TableField("progress_advance_count")
    private Integer progressAdvanceCount;

    @TableField("converted_count")
    private Integer convertedCount;

    @TableField("task_completed_count")
    private Integer taskCompletedCount;

    @TableField("progress_details")
    private String progressDetails;

    @TableField("converted_details")
    private String convertedDetails;

    @TableField("task_completed_details")
    private String taskCompletedDetails;

    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Integer isDeleted;
}
