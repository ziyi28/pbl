package com.pbl.campus.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserRole {

    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    UserRole(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
