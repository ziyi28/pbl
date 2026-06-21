package com.pbl.campus.controller;

import com.pbl.campus.common.Result;
import com.pbl.campus.dto.response.NotificationResponse;
import com.pbl.campus.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知接口", description = "站内通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取当前用户通知列表")
    public Result<List<NotificationResponse>> getNotifications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return notificationService.getNotifications(userId);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Integer> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    public Result<Void> markAsRead(Authentication authentication,
                                   @Parameter(description = "通知ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return notificationService.markAsRead(id, userId);
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记为已读")
    public Result<Void> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return notificationService.markAllAsRead(userId);
    }
}
