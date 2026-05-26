package com.pbl.campus.controller;

import com.pbl.campus.common.Result;
import com.pbl.campus.dto.request.UserUpdateRequest;
import com.pbl.campus.dto.response.EventResponse;
import com.pbl.campus.dto.response.UserResponse;
import com.pbl.campus.service.FavoriteService;
import com.pbl.campus.service.RegistrationService;
import com.pbl.campus.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "用户信息、报名列表、收藏列表相关接口")
public class UserController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final FavoriteService favoriteService;

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的信息")
    public Result<UserResponse> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getCurrentUser(userId);
    }

    @PutMapping("/me")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    public Result<UserResponse> updateCurrentUser(Authentication authentication,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.updateCurrentUser(userId, request);
    }

    @GetMapping("/me/registrations")
    @Operation(summary = "获取我的报名", description = "获取当前用户报名的活动列表")
    public Result<List<EventResponse>> getMyRegistrations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return registrationService.getMyRegistrations(userId);
    }

    @GetMapping("/me/favorites")
    @Operation(summary = "获取我的收藏", description = "获取当前用户收藏的活动列表")
    public Result<List<EventResponse>> getMyFavorites(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getMyFavorites(userId);
    }
}
