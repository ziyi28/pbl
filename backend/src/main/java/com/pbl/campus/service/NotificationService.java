package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pbl.campus.common.Result;
import com.pbl.campus.dto.response.NotificationResponse;
import com.pbl.campus.entity.Notification;
import com.pbl.campus.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public Result<List<NotificationResponse>> getNotifications(Long userId) {
        List<Notification> notifications = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt));

        List<NotificationResponse> list = notifications.stream()
                .map(this::toResponse)
                .toList();
        return Result.success(list);
    }

    public Result<Integer> getUnreadCount(Long userId) {
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        return Result.success(count.intValue());
    }

    public Result<Void> markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.error(404, "通知不存在");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
        return Result.success(null);
    }

    public Result<Void> markAllAsRead(Long userId) {
        List<Notification> unread = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        for (Notification n : unread) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
        return Result.success(null);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setType(notification.getType());
        response.setIsRead(notification.getIsRead() != null && notification.getIsRead() == 1);
        response.setRelatedEventId(notification.getRelatedEventId());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
