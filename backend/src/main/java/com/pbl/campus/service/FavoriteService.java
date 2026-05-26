package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pbl.campus.common.Result;
import com.pbl.campus.dto.response.EventResponse;
import com.pbl.campus.entity.Event;
import com.pbl.campus.entity.Favorite;
import com.pbl.campus.mapper.EventMapper;
import com.pbl.campus.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final EventMapper eventMapper;

    public Result<Void> addFavorite(Long userId, Long eventId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error("活动不存在");
        }

        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getEventId, eventId));
        if (count > 0) {
            return Result.error("已收藏该活动");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setEventId(eventId);
        favoriteMapper.insert(favorite);

        return Result.success("收藏成功", null);
    }

    public Result<Void> removeFavorite(Long userId, Long eventId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || event.getIsDeleted()) {
            return Result.error("活动不存在");
        }

        Favorite favorite = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getEventId, eventId));
        if (favorite == null) {
            return Result.error("未收藏该活动");
        }

        favoriteMapper.deleteById(favorite.getId());
        return Result.success("已取消收藏", null);
    }

    public Result<List<EventResponse>> getMyFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt));

        List<EventResponse> events = favorites.stream()
                .map(f -> {
                    Event event = eventMapper.selectById(f.getEventId());
                    if (event == null || event.getIsDeleted()) return null;
                    EventResponse response = new EventResponse();
                    BeanUtils.copyProperties(event, response);
                    return response;
                })
                .filter(e -> e != null)
                .toList();

        return Result.success(events);
    }
}
