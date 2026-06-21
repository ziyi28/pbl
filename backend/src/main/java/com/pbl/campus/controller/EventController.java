package com.pbl.campus.controller;

import com.pbl.campus.common.PageResult;
import com.pbl.campus.common.Result;
import com.pbl.campus.common.enums.EventCategory;
import com.pbl.campus.common.enums.EventStatus;
import com.pbl.campus.dto.request.CommentCreateRequest;
import com.pbl.campus.dto.request.EventCreateRequest;
import com.pbl.campus.dto.request.EventUpdateRequest;
import com.pbl.campus.dto.response.CommentResponse;
import com.pbl.campus.dto.response.EventResponse;
import com.pbl.campus.dto.response.ParticipantResponse;
import com.pbl.campus.service.CommentService;
import com.pbl.campus.service.EventService;
import com.pbl.campus.service.FavoriteService;
import com.pbl.campus.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "活动接口", description = "活动CRUD、报名、评论、收藏相关接口")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CommentService commentService;
    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "查询活动列表", description = "分页查询活动列表，支持分类、状态、关键词筛选。登录时返回当前用户的报名/收藏状态。")
    public Result<PageResult<EventResponse>> listEvents(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "活动分类") @RequestParam(required = false) EventCategory category,
            @Parameter(description = "活动状态") @RequestParam(required = false) EventStatus status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return eventService.listEvents(page, size, category, status, keyword, userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询活动详情", description = "根据ID查询活动详情")
    public Result<EventResponse> getEvent(@Parameter(description = "活动ID") @PathVariable Long id, Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return eventService.getEvent(id, userId);
    }

    @PostMapping
    @Operation(summary = "创建活动", description = "管理员创建新活动")
    public Result<EventResponse> createEvent(Authentication authentication,
                                              @Valid @RequestBody EventCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return eventService.createEvent(userId, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新活动", description = "管理员更新活动信息")
    public Result<EventResponse> updateEvent(Authentication authentication,
                                             @Parameter(description = "活动ID") @PathVariable Long id,
                                             @Valid @RequestBody EventUpdateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return eventService.updateEvent(id, userId, isAdmin, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动", description = "管理员删除活动")
    public Result<Void> deleteEvent(Authentication authentication, @Parameter(description = "活动ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return eventService.deleteEvent(id, userId, isAdmin);
    }

    @PostMapping("/{id}/registrations")
    @Operation(summary = "报名活动", description = "用户报名参加活动")
    public Result<Void> register(Authentication authentication, @Parameter(description = "活动ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return registrationService.register(userId, id);
    }

    @DeleteMapping("/{id}/registrations")
    @Operation(summary = "取消报名", description = "用户取消活动报名")
    public Result<Void> cancelRegistration(Authentication authentication, @Parameter(description = "活动ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return registrationService.cancelRegistration(userId, id);
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "获取活动参与者", description = "获取活动的所有参与者列表")
    public Result<List<ParticipantResponse>> getEventParticipants(Authentication authentication,
                                                                  @Parameter(description = "活动ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return registrationService.getEventParticipants(id, userId, isAdmin);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "获取活动评论", description = "分页获取活动的评论列表")
    public Result<PageResult<CommentResponse>> listComments(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认20") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return commentService.listComments(id, page, size, userId);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "发表评论", description = "用户对活动发表评论")
    public Result<CommentResponse> createComment(Authentication authentication,
                                                  @Parameter(description = "活动ID") @PathVariable Long id,
                                                  @Valid @RequestBody CommentCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return commentService.createComment(userId, id, request);
    }

    @PostMapping("/{id}/favorites")
    @Operation(summary = "收藏活动", description = "用户收藏活动")
    public Result<Void> addFavorite(Authentication authentication, @Parameter(description = "活动ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.addFavorite(userId, id);
    }

    @DeleteMapping("/{id}/favorites")
    @Operation(summary = "取消收藏", description = "用户取消活动收藏")
    public Result<Void> removeFavorite(Authentication authentication, @Parameter(description = "活动ID") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.removeFavorite(userId, id);
    }
}
