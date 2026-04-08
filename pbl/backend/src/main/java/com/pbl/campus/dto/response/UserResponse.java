package com.pbl.campus.dto.response;

import com.pbl.campus.common.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private UserRole role;
    private LocalDateTime createdAt;
}
