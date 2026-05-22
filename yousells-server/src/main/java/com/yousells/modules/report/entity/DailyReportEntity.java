package com.yousells.modules.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("daily_reports")
public class DailyReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate reportDate;
    private Long userId;
    private String summary;
    private String issues;
    private String tomorrowPlan;
    private Integer newCustomerCount;
    private Integer followUpCount;
    private Integer progressAdvanceCount;
    private Integer taskCompletedCount;
    private String progressDetails;
    private String taskCompletedDetails;

    @TableField(exist = false)
    private LocalDateTime createdAt;
    @TableField(exist = false)
    private LocalDateTime updatedAt;
    @TableField(exist = false)
    private Long createdBy;
    @TableField(exist = false)
    private Long updatedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getIssues() { return issues; }
    public void setIssues(String issues) { this.issues = issues; }

    public String getTomorrowPlan() { return tomorrowPlan; }
    public void setTomorrowPlan(String tomorrowPlan) { this.tomorrowPlan = tomorrowPlan; }

    public Integer getNewCustomerCount() { return newCustomerCount; }
    public void setNewCustomerCount(Integer newCustomerCount) { this.newCustomerCount = newCustomerCount; }

    public Integer getFollowUpCount() { return followUpCount; }
    public void setFollowUpCount(Integer followUpCount) { this.followUpCount = followUpCount; }

    public Integer getProgressAdvanceCount() { return progressAdvanceCount; }
    public void setProgressAdvanceCount(Integer progressAdvanceCount) { this.progressAdvanceCount = progressAdvanceCount; }

    public Integer getTaskCompletedCount() { return taskCompletedCount; }
    public void setTaskCompletedCount(Integer taskCompletedCount) { this.taskCompletedCount = taskCompletedCount; }

    public String getProgressDetails() { return progressDetails; }
    public void setProgressDetails(String progressDetails) { this.progressDetails = progressDetails; }

    public String getTaskCompletedDetails() { return taskCompletedDetails; }
    public void setTaskCompletedDetails(String taskCompletedDetails) { this.taskCompletedDetails = taskCompletedDetails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
}
