package com.yousells.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.websocket.NotificationWebSocketHandler;
import com.yousells.modules.notification.entity.NotificationEntity;
import com.yousells.modules.notification.mapper.NotificationMapper;
import com.yousells.modules.notification.service.NotificationService;
import com.yousells.modules.notification.vo.NotificationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public void sendNotification(NotificationEntity notification) {
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        // WebSocket 实时推送
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "notification");
        payload.put("data", toVo(notification));
        webSocketHandler.sendToUser(notification.getUserId(), payload);
    }

    @Override
    public Page<NotificationVo> pageNotifications(Long userId, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        Page<NotificationEntity> entityPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getIsDeleted, 0)
                .orderByDesc(NotificationEntity::getCreatedAt);
        Page<NotificationEntity> result = notificationMapper.selectPage(entityPage, wrapper);

        List<NotificationVo> list = result.getRecords().stream()
                .map(this::toVo)
                .toList();
        Page<NotificationVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(list);
        return voPage;
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        NotificationEntity entity = notificationMapper.selectById(notificationId);
        if (entity == null || !entity.getUserId().equals(userId)) {
            return;
        }
        entity.setIsRead(1);
        notificationMapper.updateById(entity);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        notificationMapper.softDelete(notificationId, userId);
    }

    @Override
    public Page<NotificationVo> pageTrashNotifications(Long userId, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        int offset = (page - 1) * pageSize;
        List<NotificationEntity> records = notificationMapper.pageTrash(userId, offset, pageSize);
        int total = notificationMapper.countTrash(userId);

        List<NotificationVo> list = records.stream()
                .map(this::toVo)
                .toList();
        Page<NotificationVo> voPage = new Page<>(page, pageSize, total);
        voPage.setRecords(list);
        return voPage;
    }

    @Override
    public void restoreNotification(Long userId, Long notificationId) {
        notificationMapper.restore(notificationId, userId);
    }

    @Override
    public void permanentDelete(Long userId, Long notificationId) {
        notificationMapper.permanentDelete(notificationId, userId);
    }

    @Override
    public void permanentDeleteAll(Long userId) {
        notificationMapper.permanentDeleteAll(userId);
    }

    private NotificationVo toVo(NotificationEntity entity) {
        NotificationVo vo = new NotificationVo();
        vo.setId(entity.getId());
        vo.setType(entity.getType());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessId(entity.getBusinessId());
        vo.setIsRead(entity.getIsRead());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
