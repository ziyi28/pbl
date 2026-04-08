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
import com.pbl.campus.service.CommentService;
import com.pbl.campus.service.EventService;
import com.pbl.campus.service.FavoriteService;
import com.pbl.campus.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CommentService commentService;
    private final FavoriteService favoriteService;

    // ========== 活动 CRUD ==========

    @GetMapping
    public Result<PageResult<EventResponse>> listEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String keyword) {
        return eventService.listEvents(page, size, category, status, keyword);
    }

    @GetMapping("/{id}")
    public Result<EventResponse> getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @PostMapping
    public Result<EventResponse> createEvent(Authentication authentication,
                                              @Valid @RequestBody EventCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return eventService.createEvent(userId, request);
    }

    @PutMapping("/{id}")
    public Result<EventResponse> updateEvent(@PathVariable Long id,
                                              @Valid @RequestBody EventUpdateRequest request) {
        return eventService.updateEvent(id, request);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEvent(@PathVariable Long id) {
        return eventService.deleteEvent(id);
    }

    // ========== 报名 ==========

    @PostMapping("/{id}/registrations")
    public Result<Void> register(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return registrationService.register(userId, id);
    }

    @DeleteMapping("/{id}/registrations")
    public Result<Void> cancelRegistration(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return registrationService.cancelRegistration(userId, id);
    }

    // ========== 评论 ==========

    @GetMapping("/{id}/comments")
    public Result<PageResult<CommentResponse>> listComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return commentService.listComments(id, page, size);
    }

    @PostMapping("/{id}/comments")
    public Result<CommentResponse> createComment(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody CommentCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return commentService.createComment(userId, id, request);
    }

    // ========== 收藏 ==========

    @PostMapping("/{id}/favorites")
    public Result<Void> addFavorite(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.addFavorite(userId, id);
    }

    @DeleteMapping("/{id}/favorites")
    public Result<Void> removeFavorite(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.removeFavorite(userId, id);
    }
}
