package com.pbl.campus.controller;

import com.pbl.campus.common.Result;
import com.pbl.campus.dto.request.LoginRequest;
import com.pbl.campus.dto.request.RegisterRequest;
import com.pbl.campus.dto.response.LoginResponse;
import com.pbl.campus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
