package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pbl.campus.common.Result;
import com.pbl.campus.common.enums.EventStatus;
import com.pbl.campus.dto.response.EventResponse;
import com.pbl.campus.dto.response.ParticipantResponse;
import com.pbl.campus.entity.Event;
import com.pbl.campus.entity.Notification;
import com.pbl.campus.entity.Registration;
import com.pbl.campus.entity.User;
import com.pbl.campus.mapper.EventMapper;
import com.pbl.campus.mapper.NotificationMapper;
import com.pbl.campus.mapper.RegistrationMapper;
import com.pbl.campus.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    @Transactional
    public Result<Void> register(Long userId, Long eventId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error("活动不存在");
        }
        if (event.getStatus() != EventStatus.OPEN) {
            if (event.getStatus() == EventStatus.CANCELLED) {
                return Result.error("该活动已取消，不可报名");
            }
            return Result.error("该活动当前不在报名阶段");
        }
        if (event.getRegistrationDeadline().isBefore(LocalDateTime.now())) {
            return Result.error("报名已截止");
        }
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            return Result.error("名额已满");
        }

        // 检查是否已报名
        Long count = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getEventId, eventId));
        if (count > 0) {
            return Result.error("您已报名该活动");
        }

        // 乐观锁更新名额（先到先得，冲突表示名额已被抢光）
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        int rows = eventMapper.updateById(event);
        if (rows == 0) {
            return Result.error("手慢了一步，名额已被抢光");
        }

        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setEventId(eventId);
        registrationMapper.insert(registration);

        // 发送报名成功通知
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("报名成功");
        notification.setContent("您已成功报名活动「" + event.getTitle() + "」");
        notification.setType("REGISTRATION_SUCCESS");
        notification.setRelatedEventId(eventId);
        notificationMapper.insert(notification);

        return Result.success("报名成功", null);
    }

    @Transactional
    public Result<Void> cancelRegistration(Long userId, Long eventId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error("活动不存在");
        }
        if (event.getStatus() != EventStatus.OPEN) {
            if (event.getStatus() == EventStatus.CANCELLED) {
                return Result.error("活动已取消，不可操作");
            }
            return Result.error("活动已开始，不可取消报名");
        }

        Registration registration = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getEventId, eventId));
        if (registration == null) {
            return Result.error("您未报名该活动");
        }

        // 先使用乐观锁更新名额，成功后再删除报名记录，保证数据一致性
        event.setCurrentParticipants(event.getCurrentParticipants() - 1);
        int rows = eventMapper.updateById(event);
        if (rows == 0) {
            // 乐观锁冲突时抛出 RuntimeException 触发事务回滚
            throw new RuntimeException("取消报名并发冲突，请重试");
        }

        registrationMapper.deleteById(registration.getId());

        // 发送取消报名通知
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("已取消报名");
        notification.setContent("您已取消活动「" + event.getTitle() + "」的报名");
        notification.setType("REGISTRATION_CANCELLED");
        notification.setRelatedEventId(eventId);
        notificationMapper.insert(notification);

        return Result.success("已取消报名", null);
    }

    public Result<List<EventResponse>> getMyRegistrations(Long userId) {
        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getUserId, userId)
                        .orderByDesc(Registration::getCreatedAt));

        List<EventResponse> events = registrations.stream()
                .map(r -> {
                    Event event = eventMapper.selectById(r.getEventId());
                    if (event == null) return null;
                    EventResponse response = new EventResponse();
                    BeanUtils.copyProperties(event, response);
                    User creator = userMapper.selectById(event.getCreatorId());
                    if (creator != null) {
                        response.setCreatorName(creator.getUsername());
                    }
                    return response;
                })
                .filter(e -> e != null)
                .toList();

        return Result.success(events);
    }

    public Result<List<ParticipantResponse>> getEventParticipants(Long eventId, Long userId, boolean isAdmin) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error(404, "活动不存在");
        }
        if (!isAdmin && !event.getCreatorId().equals(userId)) {
            return Result.error(403, "无权查看参与者列表");
        }

        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getEventId, eventId)
                        .orderByDesc(Registration::getCreatedAt));

        List<ParticipantResponse> participants = registrations.stream()
                .map(r -> {
                    User user = userMapper.selectById(r.getUserId());
                    if (user == null) return null;
                    ParticipantResponse response = new ParticipantResponse();
                    response.setUserId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setAvatar(user.getAvatar());
                    response.setEmail(user.getEmail());
                    response.setRegisteredAt(r.getCreatedAt());
                    return response;
                })
                .filter(p -> p != null)
                .toList();

        return Result.success(participants);
    }

    /**
     * 导出报名名单为CSV（含UTF-8 BOM，兼容Excel中文）
     */
    public void exportParticipants(Long eventId, Long userId, boolean isAdmin, HttpServletResponse response) throws IOException {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            writeExportError(response, 404, "活动不存在");
            return;
        }
        if (!isAdmin && !event.getCreatorId().equals(userId)) {
            writeExportError(response, 403, "无权导出报名名单");
            return;
        }

        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getEventId, eventId)
                        .orderByDesc(Registration::getCreatedAt));

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"participants-" + eventId + ".csv\"");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        // UTF-8 BOM for Excel compatibility
        writer.write('﻿');
        writer.println("活动ID,活动标题,用户ID,用户名,邮箱,报名时间");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Registration reg : registrations) {
            User user = userMapper.selectById(reg.getUserId());
            if (user == null) continue;
            writer.printf("%d,\"%s\",%d,\"%s\",\"%s\",\"%s\"%n",
                    eventId,
                    escapeCsv(event.getTitle()),
                    user.getId(),
                    escapeCsv(user.getUsername()),
                    user.getEmail() != null ? escapeCsv(user.getEmail()) : "",
                    reg.getCreatedAt() != null ? "\t" + reg.getCreatedAt().format(formatter) : "");
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private void writeExportError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
