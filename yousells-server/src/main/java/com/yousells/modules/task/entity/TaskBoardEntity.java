package com.yousells.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yousells.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("task_boards")
public class TaskBoardEntity extends BaseEntity {

    @TableField("task_title")
    private String taskTitle;

    @TableField("task_description")
    private String taskDescription;

    @TableField("direction")
    private String direction;

    @TableField("owner_user_id")
    private Long ownerUserId;

    @TableField("creator_user_id")
    private Long creatorUserId;

    @TableField("suggested_to_user_id")
    private Long suggestedToUserId;

    @TableField("status")
    private String status;

    @TableField("priority")
    private String priority;

    @TableField("due_at")
    private LocalDateTime dueAt;
}
