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
        if (request.getRegistrationDeadline().isAfter(request.getStartTime())) {
            return Result.error("报名截止时间不能晚于开始时间");
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
        EventResponse response = toResponse(event);
        if (userId != null) {
            response.setIsRegistered(registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getUserId, userId)
                    .eq(Registration::getEventId, id)) > 0);
            response.setIsFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, userId)
                    .eq(Favorite::getEventId, id)) > 0);
        }
        return Result.success(response);
    }

    public Result<PageResult<EventResponse>> listEvents(int page, int size,
                                                         EventCategory category,
                                                         EventStatus status,
                                                         String keyword) {
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<Event>()
                .eq(Event::getIsDeleted, false)
                .eq(category != null, Event::getCategory, category)
                .eq(status != null, Event::getStatus, status)
                .like(StringUtils.hasText(keyword), Event::getTitle, keyword)
                .orderByDesc(Event::getCreatedAt);

        Page<Event> pageResult = eventMapper.selectPage(new Page<>(page, size), wrapper);

        List<EventResponse> records = pageResult.getRecords().stream()
                .map(this::toResponse)
                .toList();

        PageResult<EventResponse> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    public Result<EventResponse> updateEvent(Long id, EventUpdateRequest request) {
        Event event = eventMapper.selectById(id);
        if (event == null || event.getIsDeleted()) {
            return Result.error(404, "活动不存在");
        }
        if (event.getStatus() == EventStatus.ENDED) {
            return Result.error("已结束的活动不可编辑");
        }
        if (request.getMaxParticipants() != null
                && request.getMaxParticipants() < event.getCurrentParticipants()) {
            return Result.error("最大人数不能小于当前已报名人数");
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

    public Result<Void> deleteEvent(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null || event.getIsDeleted()) {
            return Result.error(404, "活动不存在");
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
        EventResponse response = new EventResponse();
        BeanUtils.copyProperties(event, response);
        User creator = userMapper.selectById(event.getCreatorId());
        if (creator != null) {
            response.setCreatorName(creator.getUsername());
        }
        return response;
    }
}
