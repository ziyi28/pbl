package com.pbl.campus.controller;

import com.pbl.campus.common.Result;
import com.pbl.campus.dto.request.LoginRequest;
import com.pbl.campus.dto.request.RegisterRequest;
import com.pbl.campus.dto.response.LoginResponse;
import com.pbl.campus.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户注册、登录相关接口")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取JWT令牌")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
