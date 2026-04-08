package com.pbl.campus.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {

    private String avatar;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String bio;
}
