package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pbl.campus.common.PageResult;
import com.pbl.campus.common.Result;
import com.pbl.campus.common.enums.EventCategory;
import com.pbl.campus.common.enums.EventStatus;
import com.pbl.campus.dto.request.EventCreateRequest;
import com.pbl.campus.dto.request.EventUpdateRequest;
import com.pbl.campus.dto.response.EventResponse;
import com.pbl.campus.entity.Event;
import com.pbl.campus.entity.Favorite;
import com.pbl.campus.entity.Registration;
import com.pbl.campus.entity.User;
import com.pbl.campus.mapper.EventMapper;
import com.pbl.campus.mapper.FavoriteMapper;
import com.pbl.campus.mapper.RegistrationMapper;
import com.pbl.campus.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final RegistrationMapper registrationMapper;
    private final FavoriteMapper favoriteMapper;

    public Result<EventResponse> createEvent(Long creatorId, EventCreateRequest request) {
        // 校验时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            return Result.error("结束时间必须晚于开始时间");
        }
        if (!request.getRegistrationDeadline().isBefore(request.getStartTime())) {
            return Result.error("报名截止时间必须早于开始时间");
        }

        Event event = new Event();
        BeanUtils.copyProperties(request, event);
        event.setCreatorId(creatorId);
        event.setCurrentParticipants(0);
        event.setStatus(EventStatus.OPEN);
        event.setIsDeleted(false);
        event.setVersion(0);

        eventMapper.insert(event);

        return Result.success("活动创建成功", toResponse(event));
    }

    public Result<EventResponse> getEvent(Long id) {
        return getEvent(id, null);
    }

    public Result<EventResponse> getEvent(Long id, Long userId) {
        Event event = eventMapper.selectById(id);
        if (event == null || event.getIsDeleted()) {
            return Result.error(404, "活动不存在");
        }
        return Result.success(toResponse(event, userId));
    }

    public Result<PageResult<EventResponse>> listEvents(int page, int size,
                                                         EventCategory category,
                                                         EventStatus status,
                                                         String keyword,
                                                         boolean availableOnly,
                                                         Long userId) {
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<Event>()
                .eq(Event::getIsDeleted, false)
                .eq(category != null, Event::getCategory, category)
                .eq(status != null, Event::getStatus, status)
                .like(StringUtils.hasText(keyword), Event::getTitle, keyword)
                .orderByDesc(Event::getCreatedAt);

        // 只看可报名：OPEN + 未截止 + 未满
        if (availableOnly) {
            wrapper.eq(Event::getStatus, EventStatus.OPEN)
                   .ge(Event::getRegistrationDeadline, LocalDateTime.now())
                   .apply("current_participants < max_participants");
        }

        Page<Event> pageResult = eventMapper.selectPage(new Page<>(page, size), wrapper);

        List<EventResponse> records = pageResult.getRecords().stream()
                .map(event -> toResponse(event, userId))
                .toList();

        PageResult<EventResponse> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    public Result<EventResponse> updateEvent(Long id, Long userId, boolean isAdmin, EventUpdateRequest request) {
        Event event = eventMapper.selectById(id);
        if (event == null || event.getIsDeleted()) {
            return Result.error(404, "活动不存在");
        }
        if (!isAdmin && !event.getCreatorId().equals(userId)) {
            return Result.error(403, "无权编辑该活动");
        }
        if (event.getStatus() == EventStatus.ENDED) {
            return Result.error("已结束的活动不可编辑");
        }
        if (request.getMaxParticipants() != null
                && request.getMaxParticipants() < event.getCurrentParticipants()) {
            return Result.error("最大人数不能小于当前已报名人数");
        }

        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : event.getStartTime();
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : event.getEndTime();
        LocalDateTime registrationDeadline = request.getRegistrationDeadline() != null
                ? request.getRegistrationDeadline()
                : event.getRegistrationDeadline();

        if (endTime.isBefore(startTime)) {
            return Result.error("结束时间必须晚于开始时间");
        }
        if (!registrationDeadline.isBefore(startTime)) {
            return Result.error("报名截止时间必须早于开始时间");
        }

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategory() != null) event.setCategory(request.getCategory());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getRegistrationDeadline() != null) event.setRegistrationDeadline(request.getRegistrationDeadline());
        if (request.getMaxParticipants() != null) event.setMaxParticipants(request.getMaxParticipants());
        if (request.getCoverImage() != null) event.setCoverImage(request.getCoverImage());

        eventMapper.updateById(event);
        return Result.success("活动更新成功", toResponse(event));
    }

    public Result<Void> deleteEvent(Long id, Long userId, boolean isAdmin) {
        Event event = eventMapper.selectById(id);
        if (event == null || event.getIsDeleted()) {
            return Result.error(404, "活动不存在");
        }
        if (!isAdmin && !event.getCreatorId().equals(userId)) {
            return Result.error(403, "无权删除该活动");
        }
        eventMapper.deleteById(id);
        return Result.success("活动已删除", null);
    }

    /**
     * 定时任务：每分钟检查并自动更新活动状态
     */
    @Scheduled(fixedRate = 60000)
    public void autoUpdateEventStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 报名中 → 进行中
        eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .eq(Event::getStatus, EventStatus.OPEN)
                .eq(Event::getIsDeleted, false)
                .le(Event::getStartTime, now))
                .forEach(event -> {
                    event.setStatus(EventStatus.ONGOING);
                    eventMapper.updateById(event);
                });

        // 进行中 → 已结束
        eventMapper.selectList(new LambdaQueryWrapper<Event>()
                .eq(Event::getStatus, EventStatus.ONGOING)
                .eq(Event::getIsDeleted, false)
                .le(Event::getEndTime, now))
                .forEach(event -> {
                    event.setStatus(EventStatus.ENDED);
                    eventMapper.updateById(event);
                });
    }

    private EventResponse toResponse(Event event) {
        return toResponse(event, null);
    }

    private EventResponse toResponse(Event event, Long userId) {
        EventResponse response = new EventResponse();
        BeanUtils.copyProperties(event, response);
        User creator = userMapper.selectById(event.getCreatorId());
        if (creator != null) {
            response.setCreatorName(creator.getUsername());
        }
        if (userId != null) {
            response.setIsRegistered(registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getUserId, userId)
                    .eq(Registration::getEventId, event.getId())) > 0);
            response.setIsFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, userId)
                    .eq(Favorite::getEventId, event.getId())) > 0);
        }
        return response;
    }
}
