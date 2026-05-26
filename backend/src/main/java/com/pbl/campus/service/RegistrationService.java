package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pbl.campus.common.Result;
import com.pbl.campus.common.enums.EventStatus;
import com.pbl.campus.dto.response.EventResponse;
import com.pbl.campus.dto.response.ParticipantResponse;
import com.pbl.campus.entity.Event;
import com.pbl.campus.entity.Registration;
import com.pbl.campus.entity.User;
import com.pbl.campus.mapper.EventMapper;
import com.pbl.campus.mapper.RegistrationMapper;
import com.pbl.campus.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Transactional
    public Result<Void> register(Long userId, Long eventId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error("活动不存在");
        }
        if (event.getStatus() != EventStatus.OPEN) {
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

        // 乐观锁更新名额
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        int rows = eventMapper.updateById(event);
        if (rows == 0) {
            return Result.error("报名失败，请重试");
        }

        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setEventId(eventId);
        registrationMapper.insert(registration);

        return Result.success("报名成功", null);
    }

    @Transactional
    public Result<Void> cancelRegistration(Long userId, Long eventId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error("活动不存在");
        }
        if (event.getStatus() != EventStatus.OPEN) {
            return Result.error("活动已开始，不可取消报名");
        }

        Registration registration = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getEventId, eventId));
        if (registration == null) {
            return Result.error("您未报名该活动");
        }

        registrationMapper.deleteById(registration.getId());

        event.setCurrentParticipants(event.getCurrentParticipants() - 1);
        int rows = eventMapper.updateById(event);
        if (rows == 0) {
            return Result.error("取消失败，请重试");
        }

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
}
