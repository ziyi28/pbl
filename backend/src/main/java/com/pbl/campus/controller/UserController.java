package com.pbl.campus.controller;

import com.pbl.campus.common.Result;
import com.pbl.campus.dto.request.UserUpdateRequest;
import com.pbl.campus.dto.response.EventResponse;
import com.pbl.campus.dto.response.UserResponse;
import com.pbl.campus.service.FavoriteService;
import com.pbl.campus.service.RegistrationService;
import com.pbl.campus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final FavoriteService favoriteService;

    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getCurrentUser(userId);
    }

    @PutMapping("/me")
    public Result<UserResponse> updateCurrentUser(Authentication authentication,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.updateCurrentUser(userId, request);
    }

    @GetMapping("/me/registrations")
    public Result<List<EventResponse>> getMyRegistrations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return registrationService.getMyRegistrations(userId);
    }

    @GetMapping("/me/favorites")
    public Result<List<EventResponse>> getMyFavorites(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getMyFavorites(userId);
    }
}
