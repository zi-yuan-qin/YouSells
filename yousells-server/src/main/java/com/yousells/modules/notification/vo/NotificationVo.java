package com.yousells.modules.notification.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVo {

    private Long id;
    private String type;
    private String title;
    private String content;
    private String businessType;
    private Long businessId;
    private Integer isRead;
    private Integer isDeleted;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
