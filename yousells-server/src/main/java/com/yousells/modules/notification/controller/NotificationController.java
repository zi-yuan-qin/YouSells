package com.yousells.modules.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.common.response.ApiResponse;
import com.yousells.common.response.PageResponse;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.notification.service.NotificationService;
import com.yousells.modules.notification.vo.NotificationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationVo>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        Page<NotificationVo> result = notificationService.pageNotifications(userId, page, pageSize);
        PageResponse<NotificationVo> response = new PageResponse<>(
                result.getRecords(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                (int) result.getTotal()
        );
        return ApiResponse.success(response);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount() {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        notificationService.markRead(userId, id);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        notificationService.markAllRead(userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        notificationService.deleteNotification(userId, id);
        return ApiResponse.success();
    }

    @GetMapping("/trash")
    public ApiResponse<PageResponse<NotificationVo>> pageTrash(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        Page<NotificationVo> result = notificationService.pageTrashNotifications(userId, page, pageSize);
        PageResponse<NotificationVo> response = new PageResponse<>(
                result.getRecords(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                (int) result.getTotal()
        );
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/restore")
    public ApiResponse<Void> restoreNotification(@PathVariable Long id) {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        notificationService.restoreNotification(userId, id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> permanentDelete(@PathVariable Long id) {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        notificationService.permanentDelete(userId, id);
        return ApiResponse.success();
    }

    @DeleteMapping("/trash")
    public ApiResponse<Void> permanentDeleteAll() {
        Long userId = SecurityUserContext.requireCurrentUser().userId();
        notificationService.permanentDeleteAll(userId);
        return ApiResponse.success();
    }
}
