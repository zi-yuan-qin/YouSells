package com.yousells.modules.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.modules.notification.entity.NotificationEntity;
import com.yousells.modules.notification.vo.NotificationVo;

public interface NotificationService {

    void sendNotification(NotificationEntity notification);

    Page<NotificationVo> pageNotifications(Long userId, int page, int pageSize);

    int getUnreadCount(Long userId);

    void markRead(Long userId, Long notificationId);

    void markAllRead(Long userId);

    void deleteNotification(Long userId, Long notificationId);

    Page<NotificationVo> pageTrashNotifications(Long userId, int page, int pageSize);

    void restoreNotification(Long userId, Long notificationId);

    void permanentDelete(Long userId, Long notificationId);

    void permanentDeleteAll(Long userId);
}
