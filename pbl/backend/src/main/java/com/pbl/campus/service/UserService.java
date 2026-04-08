package com.pbl.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pbl.campus.common.Result;
import com.pbl.campus.common.enums.UserRole;
import com.pbl.campus.dto.request.LoginRequest;
import com.pbl.campus.dto.request.RegisterRequest;
import com.pbl.campus.dto.request.UserUpdateRequest;
import com.pbl.campus.dto.response.LoginResponse;
import com.pbl.campus.dto.response.UserResponse;
import com.pbl.campus.entity.User;
import com.pbl.campus.mapper.UserMapper;
import com.pbl.campus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Result<Void> register(RegisterRequest request) {
        // 检查用户名重复
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())) > 0) {
            return Result.error("用户名已被注册");
        }
        // 检查邮箱重复
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())) > 0) {
            return Result.error("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRole.USER);
        userMapper.insert(user);

        return Result.success("注册成功", null);
    }

    public Result<LoginResponse> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole().getValue());
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getRole().getValue());
        return Result.success("登录成功", response);
    }

    public Result<UserResponse> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return Result.success(response);
    }

    public Result<UserResponse> updateCurrentUser(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (request.getEmail() != null) {
            // 检查新邮箱是否已被其他用户使用
            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getEmail())
                    .ne(User::getId, userId));
            if (existing != null) {
                return Result.error("邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userMapper.updateById(user);

        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return Result.success(response);
    }
}
